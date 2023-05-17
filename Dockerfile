FROM gradle:jdk17-focal as build
COPY padlcore padlbridge/padlcore
WORKDIR padlbridge/padlcore
RUN ./gradlew distTar
RUN find /home/gradle -name "*.tar"

FROM eclipse-temurin:17
ENV PADLBRIDGE_HOME=/opt/padlbridge
ENV PATH $PATH:${PADLBRIDGE_HOME}
RUN groupadd padl; useradd -s /bin/bash -m -g padl -G users padl; mkdir -p ${PADLBRIDGE_HOME} ${PADLBRIDGE_LDAP_HOME} ${PADLBRIDGE_BIN_HOME}
RUN mkdir -p ${PADLBRIDGE_HOME}/conf ${PADLBRIDGE_HOME}/temp
WORKDIR ${PADLBRIDGE_HOME}
COPY --from=build /home/gradle/padlbridge/padlcore/core/build/distributions/core.tar ${PADLBRIDGE_HOME}
COPY --from=build /home/gradle/padlbridge/padlcore/conf/padlbridge-example.yaml ${PADLBRIDGE_HOME}/conf
COPY --from=build /home/gradle/padlbridge/padlcore/scripts/padlbridge.sh ${PADLBRIDGE_HOME}
RUN tar xvf core.tar && rm core.tar
RUN chmod u+x padlbridge.sh
RUN chown -R padl:padl ${PADLBRIDGE_HOME}
USER padl
# Entrypoint
ENTRYPOINT [ "/bin/bash", "-c" ]
CMD [ "${PADLBRIDGE_HOME}/padlbridge.sh", "run" ]


