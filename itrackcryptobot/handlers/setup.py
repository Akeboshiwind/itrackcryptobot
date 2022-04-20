import logging

from itrackcryptobot.jobs.setup import setup_chat

from telegram import Update
from telegram.ext import CallbackContext


def set_chat_data(bot_data, chat_id, id, vs_currency, amount):
    # Get chat data
    chats = bot_data.get("chats", {})
    chat = chats.get(chat_id, {})

    # Set chat values
    chat = {
        "chat_id": chat_id,
        "id": id,
        "vs_currency": vs_currency,
        "amount": amount,
    }

    # Store new data
    chats[chat_id] = chat
    bot_data["chats"] = chats

    return chat


def setup_handler(update: Update, context: CallbackContext) -> None:
    """
    Setup the tracker for the chat
    """
    logging.info("/setup call")

    chat_id = update.effective_chat.id

    if len(context.args) < 2:
        update.message.reply_text("Expected /setup <coin id> <vs currency> [<amount>]")
        return
    if len(context.args) == 2:
        id, vs_currency = context.args
        amount = 1
    if len(context.args) > 2:
        id = context.args[0]
        vs_currency = context.args[1]
        amount = int(context.args[2])

    chat = set_chat_data(context.bot_data, chat_id, id, vs_currency, amount)
    update.message.reply_text(f"Setup {id} vs {vs_currency}")

    setup_chat(context.job_queue, chat_id, chat)
