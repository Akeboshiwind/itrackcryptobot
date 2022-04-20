# I Track Crypto Bot

A simple bot that:
- Every hour posts the current value of an amount of crypto
- Every morning posts a graph of the value over the past 24 hours


## Running it

```bash
$ export TELEGRAM_BOT_TOKEN="<your token here>"
$ export PERSISTENCE_FILE="./stats"
$ export TZ="<your timezone (pytz)>"
$ poetry install
$ poetry run python main.py
```

## Features

- Uses the CoinGecko API
- `/setup <coin id> <vs currency> [<amount>]` command
    - Configures the bot to post for the given coin id in the chat
- Only one amount tracked per chat
- Keeps track of schedules on restart
