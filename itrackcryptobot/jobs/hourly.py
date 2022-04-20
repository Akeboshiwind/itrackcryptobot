from telegram.ext import CallbackContext

from pycoingecko import CoinGeckoAPI

cg = CoinGeckoAPI()


def hourly(context: CallbackContext) -> None:
    chat_id = context.job.context["chat_id"]
    chat = context.bot_data["chats"][chat_id]

    # Extract the parameters
    id = chat["id"]
    vs_currency = chat["vs_currency"]
    amount = chat["amount"]

    # Get the current price
    price = cg.get_price(ids=id, vs_currencies=vs_currency)
    price = price[id][vs_currency]

    # Send the message
    message = f"Current price: £{price * amount}"
    context.bot.send_message(chat_id=chat_id, text=message)
