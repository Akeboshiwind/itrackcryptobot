# I Track Crypto Bot

A simple bot that posts prices hourly using the CoinGecko API.

Only tracks one conversion per chat.

## Running it

```bash
docker run --pull-always ghcr.io/akeboshiwind/itrackcryptobot:latest \
    -e "TELEGRAM_BOT_TOKEN=<your token here>" \
    -e "DATA_PATH=/data/store.edn" \
    -e "TZ=<your timezone>"
```

## Usage

Initially you'll need to run `/setup {amount} {coin-id}[->{vs-currency}]+`.
It should look something like this: `/setup 1 btc->usd->gbp` 

After that every hour an amount in the final currency will be posted to the chat,
and every day a chart will be posted with the price over the day.

### Commands

All commands are admin-only, once a global admin has been setup.

| Command | Description | Example |
| --- | --- | --- |
| `/setup {amount} {coin-id}[->{vs-currency}]+` | Registers the conversion for the chat | `/setup 100 siacoin->usd->gbp` |
| `/version` | Prints the bot's version | `/version` |
| `/stats` | Prints the store's contents | `/stats` |
| `/admin_list` | Lists the current global admins | `/admin_list` |
| `/admin_add <user>[ <user>]+` | Adds new admins | `/admin_add @me Johnny` |
| `/admin_remove <user>[ <user>]+` | Removes admins | `/admin_remove @you Johnny` |

## Release

1. Bump version in `bb.edn`
2. Tag with `v<version>`
3. `git push --tags`
