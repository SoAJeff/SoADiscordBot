from cogs.errorhandling import ErrorHandling

NESTED_TRACEBACK="""Traceback (most recent call last):
  File "/usr/local/lib/python3.12/site-packages/discord/client.py", line 504, in _run_event
    await coro(*args, **kwargs)
  File "/opt/gnomechild/cogs/errorhandling.py", line 66, in on_message
    raise ValueError(message.content)
ValueError: Module cogs.errorhandling reloaded.

During handling of the above exception, another exception occurred:

Traceback (most recent call last):
  File "/usr/local/lib/python3.12/site-packages/discord/client.py", line 509, in _run_event
    await self.on_error(event_name, *args, **kwargs)
  File "/opt/gnomechild/cogs/errorhandling.py", line 49, in on_general_error
    await self.report_error_to_webhook(traceback.format_exc(), event)
  File "/opt/gnomechild/cogs/errorhandling.py", line 56, in report_error_to_webhook
    await hook.send(contnt=error_str, username="Gnomechild Error Reporting")
          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
TypeError: Webhook.send() got an unexpected keyword argument 'contnt'"""

SINGLE_TRACEBACK="""Traceback (most recent call last):
  File "/usr/local/lib/python3.12/site-packages/discord/client.py", line 504, in _run_event
    await coro(*args, **kwargs)
  File "/opt/gnomechild/cogs/errorhandling.py", line 68, in on_message
    if "During handling of the above exception, another exception occurred:" in lines:
        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
ValueError: <@1265110844509913149> reload errorhandling"""

def test_find_exception_reason_single_exception():
    cog = ErrorHandling(None)
    assert cog.find_exception_reason(SINGLE_TRACEBACK) == "ValueError: <@1265110844509913149> reload errorhandling"

def test_find_exception_reason_nested_exception():
    cog = ErrorHandling(None)
    assert cog.find_exception_reason(NESTED_TRACEBACK) == "ValueError: Module cogs.errorhandling reloaded."