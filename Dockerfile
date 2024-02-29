FROM babashka/babashka AS babashka



FROM ubuntu AS vl-convert

# Download deps
RUN apt-get update && apt-get install -y curl unzip

# Download & extract vl-convert
RUN curl -fsSL https://github.com/vega/vl-convert/releases/download/v1.2.4/vl-convert_linux-64.zip -o vl-convert.zip \
    && unzip -p vl-convert.zip bin/vl-convert > vl-convert \
    && chmod +x vl-convert



FROM eclipse-temurin AS build

COPY --from=babashka /usr/local/bin/bb /usr/local/bin/bb

# Install deps
COPY bb.edn bb.edn
RUN bb prepare

# Build
COPY . .
RUN bb uberjar out.jar -m main



FROM babashka

WORKDIR /app

# Add vl-convert
COPY --from=vl-convert /vl-convert /usr/local/bin/vl-convert

# Add uberjar
COPY --from=build /out.jar /app/out.jar

# Add bb.edn (for /version command)
COPY bb.edn /app/bb.edn

CMD bb out.jar
