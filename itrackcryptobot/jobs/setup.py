from datetime import time
from pytz import timezone

from telegram.ext import CallbackContext

from itrackcryptobot.jobs.hourly import hourly
from itrackcryptobot.jobs.daily import daily

tz = timezone("Europe/London")


def setup_chat(job_queue, chat_id: int, chat: dict) -> None:
    hourly_name = f"hourly-{chat_id}"
    daily_name = f"daily-{chat_id}"

    # Clear up old jobs
    old_jobs = job_queue.get_jobs_by_name(hourly_name) \
        + job_queue.get_jobs_by_name(daily_name)
    for job in old_jobs:
        job.schedule_removal()

    # Hourly send price
    job_queue.run_repeating(
        hourly,
        interval=60*60,
        first=10,
        context=chat,
        name=f"hourly-{chat_id}"
    )

    # Daily plot a chart
    job_queue.run_daily(
        daily,
        time=time(10, 00, tzinfo=tz),
        context=chat,
        name=f"daily-{chat_id}"
    )


def setup_jobs(context: CallbackContext) -> None:
    for chat_id, chat in context.bot_data.get("chats", {}).items():
        setup_chat(context.job_queue, chat_id, chat)
