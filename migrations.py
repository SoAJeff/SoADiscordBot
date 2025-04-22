import os
import re
import json
import asyncio
import asyncpg
import config
from typing import TypedDict, Dict
from pathlib import Path
import uuid
import datetime
import argparse
import traceback


# Some aspects of this are adapted from RoboDanny's Launcher class
# Source: https://github.com/Rapptz/RoboDanny/blob/39af9d71ffd5f099094a05352c18b987a1dc5d04/launcher.py

class Revisions(TypedDict):
    # The version key represents the current activated version
    # So v1 means v1 is active and the next revision should be v2
    # In order for this to work the number has to be monotonically increasing
    # and have no gaps
    version: int
    revisions: list[Dict]

REVISION_FILE = re.compile(r'(?P<kind>V|U)(?P<version>[0-9]+)__(?P<description>.+).sql')

class Revision:
    __slots__ = ('kind', 'version', 'description', 'file')

    def __init__(self, *, kind: str, version: int, description: str, file: Path) -> None:
        self.kind: str = kind
        self.version: int = version
        self.description: str = description
        self.file: Path = file

    @classmethod
    def from_match(cls, match: re.Match[str], file: Path):
        return cls(
            kind=match.group('kind'), version=int(match.group('version')), description=match.group('description'), file=file
        )

class Migrations:
    def __init__(self, *, filename: str = 'migrations/revisions.json', connection: asyncpg.Connection):
        self.filename: str = filename
        self.root: Path = Path(filename).parent
        self.revisions: dict[int, Revision] = self.get_revisions()
        self.connection = connection
        self.load()

    def ensure_path(self) -> None:
        self.root.mkdir(exist_ok=True)

    def load_metadata(self) -> Revisions:
        try:
            with open(self.filename, 'r', encoding='utf-8') as fp:
                return json.load(fp)
        except FileNotFoundError:
            return {
                'version': 0,
                'revisions': []
            }

    def get_revisions(self) -> dict[int, Revision]:
        result: dict[int, Revision] = {}
        for file in self.root.glob('*.sql'):
            match = REVISION_FILE.match(file.name)
            if match is not None:
                rev = Revision.from_match(match, file)
                result[rev.version] = rev

        return result

    def dump(self) -> Revisions:
        return {
            'version': self.version,
            'revisions': self.versions
        }

    def load(self) -> None:
        self.ensure_path()
        data = self.load_metadata()
        self.version: int = data['version']
        self.versions: list[Dict] = data['revisions']

    def save(self):
        temp = f'{self.filename}.{uuid.uuid4()}.tmp'
        with open(temp, 'w', encoding='utf-8') as tmp:
            print(self.dump())
            json.dump(self.dump(), tmp)

        # atomically move the file
        os.replace(temp, self.filename)

    def is_next_revision_taken(self) -> bool:
        return self.version + 1 in self.revisions

    @property
    def ordered_revisions(self) -> list[Revision]:
        return sorted(self.revisions.values(), key=lambda r: r.version)
    
    def get_installed_date_for_revision(self, revision: int):
        return self.versions[revision - 1].get('date')

    def create_revision(self, reason: str, *, kind: str = 'V') -> Revision:
        cleaned = re.sub(r'\s', '_', reason)
        filename = f'{kind}{self.version + 1}__{cleaned}.sql'
        path = self.root / filename

        stub = (
            f'-- Revises: V{self.version}\n'
            f'-- Creation Date: {datetime.datetime.now(tz=datetime.timezone.utc)} UTC\n'
            f'-- Reason: {reason}\n\n'
        )

        with open(path, 'w', encoding='utf-8', newline='\n') as fp:
            fp.write(stub)

        self.save()
        return Revision(kind=kind, description=reason, version=self.version + 1, file=path)

    async def upgrade(self) -> int:
        ordered = self.ordered_revisions
        successes = 0
        async with self.connection.transaction():
            for revision in ordered:
                if revision.version > self.version:
                    sql = revision.file.read_text('utf-8')
                    await self.connection.execute(sql)
                    successes += 1
                    self.versions.append({'revision': revision.version, 'date': str(datetime.datetime.now(tz=datetime.timezone.utc))})

        self.version += successes
        self.save()
        return successes

    def display(self) -> None:
        ordered = self.ordered_revisions
        for revision in ordered:
            if revision.version > self.version:
                sql = revision.file.read_text('utf-8')
                print((sql))

async def ensure_db_connection_valid():
    if not config.PSQL_URI:
        raise ValueError("No PSQL_URI found in environment variables")
    try:
        connection: asyncpg.Connection = await asyncpg.connect(config.PSQL_URI)
        return connection
    except Exception as e:
        raise ValueError(f"Unable to connect to Postgres: {e}")
    
def create_arg_parser():
    parser = argparse.ArgumentParser(prog="migrations.py", description="Handles Gnomechild Database Migration Process",
                                     epilog="Warning: Migrations performed outside of this tool are done at your own risk!")
    mode = parser.add_subparsers(help="Command to run", required=True, dest='mode')
    mode.add_parser("init", help="Initialize the database by running all migrations")
    create_migration = mode.add_parser("create_migration", help="Create a new migration file with provided reason.")
    create_migration.add_argument("--reason", "-r", help="The reason for the revision", required=True)
    upgrade = mode.add_parser("upgrade", help="Run all outstanding migrations")
    upgrade.add_argument('--sql', help="Print SQL instead of running it", action='store_true', required=False)
    mode.add_parser("current", help="List the current migration the database is running at")
    log = mode.add_parser("log", help="List the history of all database migrations that have been run")
    log.add_argument('--reverse', action='store_true', help="Print in reverse order")

    return parser
    
async def main():
    
    args = create_arg_parser().parse_args()

    connection = await ensure_db_connection_valid()

    migrations = Migrations(connection=connection)

    match args.mode:
        case 'init':
            await migrations.upgrade()
        case 'create_migration':
            if migrations.is_next_revision_taken():
                print('An unapplied migration already exists for the next version, exiting.')
                print('Either add to that existing migration or upgrade the database and then create a new migration')
                return
            revision = migrations.create_revision(args.reason)
            print(f'Created revision V{revision.version}')
        case 'upgrade':
            if args.sql:
                migrations.display()
                return
            num_rev = await migrations.upgrade()
            print(f'Applied {num_rev} revisions.')
        case 'current':
            print(f'Database is currently at revision {migrations.version}, installed on {migrations.get_installed_date_for_revision(migrations.version)}')
        case 'log':
            revs = reversed(migrations.ordered_revisions) if not args.reverse else migrations.ordered_revisions
            for rev in revs:
                print(f"V{rev.version:>03}: {rev.description.replace("_", " ")}, installed on {migrations.get_installed_date_for_revision(rev.version)}")




if __name__ == "__main__":
    try:
       asyncio.run(main())
    except Exception as e:
        traceback.print_exc()
        print("Error encountered running database migration task:", e)

        