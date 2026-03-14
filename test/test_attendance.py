import pytest
from cogs.attendance import AttendanceSubmission, Attendance

class FakeBot:
    pass

sample_text = """Skill n Chill
26 Feb 2026
MandyPandy

Applejuiceaj
Firesteal918
Kashblessed
MandyPandy
Princess Rae"""

def test_attendance_formatting():
    submission = AttendanceSubmission("Skill n Chill",
                                      "26 Feb 2026",
                                      "MandyPandy",
                                      "Applejuiceaj\nFiresteal918\nKashblessed\nMandyPandy\nPrincess Rae")
    
    attendance = Attendance(FakeBot())
    produced_text = attendance.format_attendance_post(submission)

    assert sample_text == produced_text

def test_attendance_formatting_not_alphabetical():
    submission = AttendanceSubmission("Skill n Chill",
                                      "26 Feb 2026",
                                      "MandyPandy",
                                      "MandyPandy\nFiresteal918\nKashblessed\nApplejuiceaj\nPrincess Rae")
    
    attendance = Attendance(FakeBot())
    produced_text = attendance.format_attendance_post(submission)

    assert sample_text == produced_text
