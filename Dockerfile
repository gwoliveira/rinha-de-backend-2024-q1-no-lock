FROM ubuntu:jammy
COPY build/native/nativeCompile/graalvm-server /graalvm-server
CMD ["/graalvm-server"]
