import logging
import os

from itrackcryptobot.handlers.setup import setup_handler
from itrackcryptobot.handlers.stats import stats_handler
from itrackcryptobot.jobs.setup import setup_jobs

from telegram.ext import (
    Updater,
    CommandHandler,
    PicklePersistence,
)

# Enable logging
logging.basicConfig(
    format="%(levelname)-7s %(asctime)s %(name)s %(message)s",
    level=logging.INFO
)


def main() -> None:
    # >> Setup persistance

    # Don't store `user_data`, we use it as a temporary store
    persistence_location = os.environ.get("PERSISTENCE_FILE", "/data/stats")
    persistence = PicklePersistence(
        filename=persistence_location, store_user_data=False
    )

    # >> Setup the Bot
    updater = Updater(
        token=os.environ["TELEGRAM_BOT_TOKEN"],
        persistence=persistence
    )
    dispatcher = updater.dispatcher

    # >> Command Hanlders
    dispatcher.add_handler(CommandHandler("setup", setup_handler))
    dispatcher.add_handler(CommandHandler("stats", stats_handler))

    # >> Setup stored jobs
    updater.job_queue.run_once(setup_jobs, 1)

    # >> Start the Bot
    updater.start_polling()
    updater.idle()


if __name__ == "__main__":
    main()
