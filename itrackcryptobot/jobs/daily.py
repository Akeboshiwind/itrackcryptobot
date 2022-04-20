from io import BytesIO
from datetime import datetime

from telegram.ext import CallbackContext

from pycoingecko import CoinGeckoAPI
import matplotlib
import matplotlib.pyplot as plt
from matplotlib.dates import ConciseDateConverter
import matplotlib.units as munits

cg = CoinGeckoAPI()

# >> Setup Matplotlib

# Use Agg which supports server side rendering
matplotlib.use('Agg')

# Convert the units shown for datetimes
# Stolen from: https://matplotlib.org/stable/gallery/ticks/date_concise_formatter.html
formats = ['%y', '%b', '%d', '%H:%M', '%H:%M', '%S.%f']
zero_formats = [''] + formats[:-1]
zero_formats[3] = '%d-%b'
offset_formats = ['', '%Y', '%b %Y', '%d %b %Y', '%d %b %Y', '%d %b %Y %H:%M']
converter = ConciseDateConverter(
    formats=formats,
    zero_formats=zero_formats,
    offset_formats=offset_formats
)
munits.registry[datetime] = converter

# >> The job


def daily(context: CallbackContext) -> None:
    # Extract the parameters
    chat_id = context.job.context["chat_id"]
    id = context.job.context["id"]
    vs_currency = context.job.context["vs_currency"]
    amount = context.job.context["amount"]

    # Get history from coingecko
    history = cg.get_coin_market_chart_by_id(
        id=id,
        vs_currency=vs_currency,
        days=1
    )
    prices = history["prices"]
    ts, price = zip(*prices)

    # Transform the series
    ts = [datetime.utcfromtimestamp(t/1000) for t in ts]
    price = [amount * p for p in price]

    # Make plot
    fig, ax = plt.subplots()
    ax.plot(ts, price)
    ax.set(ylabel='amount (£)')
    ax.grid()

    # Save as png
    plot_file = BytesIO()
    fig.savefig(plot_file, format='png')
    plot_file.seek(0)

    # Send to chat
    context.bot.send_photo(chat_id=chat_id, photo=plot_file)
