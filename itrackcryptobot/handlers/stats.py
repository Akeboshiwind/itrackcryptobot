import logging
import json
import pkgutil
import re

from telegram import Update
from telegram.ext import CallbackContext

version_regex = re.compile(r'''^version = "([^"]*)"''', re.MULTILINE)


def version() -> str:
    """
    Get the project version by parsing the pyproject.toml file
    We have to do this because the project isn't "installed" by poetry
    """
    data = pkgutil.get_data("itrackcryptobot", "../pyproject.toml")
    match = version_regex.search(data.decode("utf-8"))
    if match:
        return match.group(1)


def stats_handler(update: Update, context: CallbackContext) -> None:
    """
    Show the stats about setup chats
    """
    logging.info("/stats call")

    update.message.reply_text(
        f"Version: {version()}"
        f"\nStats: {json.dumps(context.bot_data)}"
    )
