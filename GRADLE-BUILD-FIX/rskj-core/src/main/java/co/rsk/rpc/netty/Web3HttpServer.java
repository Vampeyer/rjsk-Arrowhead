package co.rsk.rpc.netty;

import co.rsk.config.InternalService;
import co.rsk.rpc.CorsConfiguration;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cors.CorsConfig;
import io.netty.handler.codec.http.cors.CorsHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;

public class Web3HttpServer implements InternalService {
    private static final Logger logger = LoggerFactory.getLogger(Web3HttpServer.class);

    private final InetAddress bindAddress;
    private final int port;
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private final int socketLinger;
    private final boolean reuseAddress;
    private final CorsConfiguration corsConfiguration;
    private final JsonRpcWeb3FilterHandler jsonRpcWeb3FilterHandler;
    private final JsonRpcWeb3ServerHandler jsonRpcWeb3ServerHandler;
    private final int maxAggregatedFrameSize;

    public Web3HttpServer(InetAddress bindAddress,
                          int port,
                          int socketLinger,
                          boolean reuseAddress,
                          CorsConfiguration corsConfiguration,
                          JsonRpcWeb3FilterHandler jsonRpcWeb3FilterHandler,
                          JsonRpcWeb3ServerHandler jsonRpcWeb3ServerHandler,
                          int maxAggregatedFrameSize) {
        this.bindAddress = bindAddress;
        this.port = port;
        this.socketLinger = socketLinger;
        this.reuseAddress = reuseAddress;
        this.corsConfiguration = corsConfiguration;
        this.jsonRpcWeb3FilterHandler = jsonRpcWeb3FilterHandler;
        this.jsonRpcWeb3ServerHandler = jsonRpcWeb3ServerHandler;
        this.bossGroup = new NioEventLoopGroup();
        this.workerGroup = new NioEventLoopGroup();
        this.maxAggregatedFrameSize = maxAggregatedFrameSize;
    }

    @Override
    public void start() {
        logger.info("RPC HTTP enabled");

        ServerBootstrap b = new ServerBootstrap();
        b.option(ChannelOption.SO_LINGER, socketLinger);
        b.option(ChannelOption.SO_REUSEADDR, reuseAddress);
        b.group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel.class)
            .handler(new LoggingHandler(LogLevel.INFO))
            .childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline p = ch.pipeline();
                    p.addLast(new HttpRequestDecoder());
                    p.addLast(new HttpResponseEncoder());
                    p.addLast(new HttpObjectAggregator(maxAggregatedFrameSize));
                    p.addLast(new HttpContentCompressor());
                    if (corsConfiguration.hasHeader()) {
                        p.addLast(new CorsHandler(
                            CorsConfig
                                .withOrigin(corsConfiguration.getHeader())
                                .allowedRequestHeaders(HttpHeaders.Names.CONTENT_TYPE)
                                .allowedRequestMethods(HttpMethod.POST)
                            .build())
                        );
                    }
                    p.addLast(new LogTracingFilterHandler());
                    p.addLast(jsonRpcWeb3FilterHandler);
                    p.addLast(new Web3HttpMethodFilterHandler());
                    p.addLast(jsonRpcWeb3ServerHandler);
                    p.addLast(buildWeb3ResultHttpResponseHandler());
                }
            });
        try {
            b.bind(bindAddress, port).sync();
        } catch (InterruptedException e) {
            logger.error("The RPC HTTP server couldn't be started", e);
            Thread.currentThread().interrupt();
        }
    }

    private Web3ResultHttpResponseHandler buildWeb3ResultHttpResponseHandler() {
        Web3HttpStatusCodeProvider web3HttpStatusCodeProvider = new Web3HttpStatusCodeProvider();
        return new Web3ResultHttpResponseHandler(web3HttpStatusCodeProvider);
    }

    @Override
    public void stop() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
