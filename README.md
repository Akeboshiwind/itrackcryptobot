# I Track Crypto Bot

A simple bot that:
- Every hour posts the current value of an amount of crypto
- Every morning posts a graph of the value over the past 24 hours


## Running it

```bash
$ export TELEGRAM_BOT_TOKEN="<your token here>"
$ export DATA_PATH="./stats"
$ export TZ="<your timezone>"
$ bb -m main
```

## Features

- Uses the CoinGecko API
- `/setup {amount} {coin-id}[->{vs-currency}]+` command
    - Configures the bot to post for the given coin id in the chat
- Only one amount tracked per chat
- Keeps track of schedules on restart
