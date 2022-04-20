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
    previous_price = chat.get("previous_price", None)

    # Get the current price
    price = cg.get_price(ids=id, vs_currencies=vs_currency)
    price = price[id][vs_currency]

    # Build message
    message = f"Current price: £{price * amount:.2f}"
    if previous_price:
        if previous_price < price:
            message += " 🔼"
        elif previous_price > price:
            message += " 🔽"

    # Update the previous_price
    chat["previous_price"] = price
    context.bot_data["chats"][chat_id] = chat

    # Send message
    context.bot.send_message(chat_id=chat_id, text=message)
