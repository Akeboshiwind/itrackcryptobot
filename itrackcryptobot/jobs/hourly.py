from telegram.ext import CallbackContext

from pycoingecko import CoinGeckoAPI

cg = CoinGeckoAPI()


def hourly(context: CallbackContext) -> None:
    # Extract the parameters
    chat_id = context.job.context["chat_id"]
    id = context.job.context["id"]
    vs_currency = context.job.context["vs_currency"]
    amount = context.job.context["amount"]

    # Get the current price
    price = cg.get_price(ids=id, vs_currencies=vs_currency)
    price = price[id][vs_currency]

    # Send the message
    message = f"Current price: £{price * amount}"
    context.bot.send_message(chat_id=chat_id, text=message)
