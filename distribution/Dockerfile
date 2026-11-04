FROM eclipse-temurin:25-jre

ARG ARCH=x64
ARG DIST_FILE=dist/condation-server-linux-${ARCH}-2026.05.tar.gz
WORKDIR /opt

COPY ${DIST_FILE} /tmp/
RUN mkdir -p /opt/cms-server \
    && tar -xzf /tmp/$(basename ${DIST_FILE}) -C /opt/cms-server --strip-components=1 \
    && rm /tmp/$(basename ${DIST_FILE})

RUN apt-get update && \
    apt-get install -y --no-install-recommends libvips-tools && \
    rm -rf /var/lib/apt/lists/*

# user anlegen
RUN groupadd -r condation && useradd -r -g condation condation

# ownership
RUN chown -R condation:condation /opt/cms-server

WORKDIR /opt/cms-server
COPY docker/server.toml /opt/cms-server/server.toml

# switch user
USER condation

RUN chmod +x ./server.sh


VOLUME ["/opt/cms-server/themes", "/opt/cms-server/modules", "/opt/cms-server/hosts/demo"]
EXPOSE 2020

ENTRYPOINT ["./server.sh"]
CMD ["server", "start"]

# Build examples:
# docker build --build-arg ARCH=x64 -t condation-server:x64 .
# docker build --build-arg ARCH=aarch64 -t condation-server:arm64 .