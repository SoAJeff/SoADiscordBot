from enum import Enum

class ForumRank(Enum):
    ONTARI = 22, "Ontari", "Deputy Owner"
    ELDAR = 4, "Eldar", "Deputy Owner"
    LIAN = 7, "Lian", "Overseer"
    ADMINISTRATOR = 34, "Administrator", "Overseer"
    ARQUENDI = 6, "Arquendi", "Coordinator"
    ADELE = 31, "Adele", "Coordinator"
    VORONWE = 44, "Voronwë", "Organiser"
    ELENDUR = 37, "Elendur", "Admin"
    SADRON = 24, "Sadron", "General"
    ATHEM = 8, "Athem", "Captain"
    MYRTH = 9, "Myrth", "Lieutenant"
    BAEL = 15, "Bael", "Sergeant"
    TYLAR = 10, "Tylar", "Corporal"
    APPLICANT = 36, "Applicant", "Recruit"
    REGISTERED = 3, "Registered User", "Recruit"

    def __new__(cls, *args, **kwds):
        value = len(cls.__members__) + 1
        obj = object.__new__(cls)
        obj._value_ = value
        return obj
    
    def __init__(self, id: int, name: str, clan_chat_rank: str):
        self._id = id
        self._name = name
        self._clan_chat_rank = clan_chat_rank
    
    @property
    def id(self):
        return self._id
    
    @property
    def name(self):
        return self._name
    
    @property
    def clan_chat_rank(self):
        return self._clan_chat_rank