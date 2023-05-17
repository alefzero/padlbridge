FROM gradle:jdk17-focal as build
RUN git clone https://github.com/alefzero/padlbridge.git
WORKDIR padlbridge/padlcore
RUN pwd && ./gradlew build

FROM eclipse-temurin:17
RUN mkdir -p /opt/padlbridge/conf /opt/padlbridge/temp
WORKDIR /opt/padlbridge
COPY --from=build /home/gradle/padlbridge/padlcore/core/build/distributions/app.tar /opt/padlbridge
RUN tar xvf app.tar


