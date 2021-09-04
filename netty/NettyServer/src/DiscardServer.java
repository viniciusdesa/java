import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class DiscardServer {
    private int port;

    public DiscardServer(int port) {
        this.port = port;
    }

    public void run() throws Exception {
        /**
         * NioEventLoopGroup é um multithreaded event loop que manipula a operação I/O.
         * Netty fornece várias implementações EventLoopGroup para diferentes tipos de transportes.
         * Estamos implementando uma aplicação server-side neste exemplo, e ainda assim dois NioEventLoopGroup serão
         * usados. O primeiro, chamado 'boss', aceita uma conexão entrante. O segundo, frequentemente chamado 'worker',
         * lida com o tráfego da conexão aceita uma vez que o boss aceite a conexão e registre-a ao worker. Quantas
         * Threads são usadas e como elas são mapeadas aos Channels criados depende da implementação do EventLoopGroup e
         * pode ser configurável via construtor.
         */
        EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            /**
             * O ServerBootstrap é uma classe helper que faz o setup de um servidor. Você pode fazer o setup do servidor
             * usando um Channel diretamente. Entretanto, note que isto é um processo tedioso, e você não precisa fazer
             * isso na maioria dos casos.
             */
            ServerBootstrap b = new ServerBootstrap(); // (2)
            b.group(bossGroup, workerGroup)
                    /**
                     * Aqui especificamos para usar o NioServerSocketChannel que é usado pra instanciar um novo Channel
                     * para aceitar conexões entrantes.
                     */
                    .channel(NioServerSocketChannel.class) // (3)
                    /**
                     * O handler especificado aqui sempre será avaliado por um Channel aceito recentemente.
                     * O ChannelInitializer é um handler especial cujo propósito é ajudar um usuário a configurar um
                     * novo Channel. É mais provável que você queira configurar o ChannelPipeline de um novo Channel
                     * adicionando alguns handlers tais como o DiscardServerHandler para implementar sua aplicação
                     * de rede. Conforme o aplicativo fica complicado, é provável que você queira adicinar mais handlers
                     * ao pipeline e, eventualmente, extraia esta classe anônima em uma classe top-level.
                     */
                    .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            //ch.pipeline().addLast(new EchoServerHandler());
                            ch.pipeline().addLast(new TimeServerHandler());
                        }
                    })
                    /**
                     * Você pode também confgurar os parâmetros que são específicos à implementação do Channel.
                     * Estamos escrevendo um servidor TCP/IP, então estamos permitidos a configurar as opções socket
                     * tais como tcpNoDelay e keepAlive. Por favor, refira aos apidocs do ChannelOption e às
                     * implementações específicas do ChannelConfig para obter uma visão geral sobre as ChannelOptions.
                     */
                    .option(ChannelOption.SO_BACKLOG, 128)          // (5)
                    /**
                     * Você notou option() e childOption()? option() é para o NioServerSocketChannel que aceita
                     * conexões entrantes. childOption() é para o Channels aceito pelo ServerChannel pai, que é o
                     * NioSocketChannel neste caso.
                     */
                    .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)

            /**
             * Estamos prontos agora. O que falta é atrelar à porta e iniciar o servidor. Aqui, atrelamos à porta 8080
             * de todas as NICs (network interface cards) na máquina. Você pode chamar o método bind() quantas vezes
             * quiser (com diferentes endereços)
             */
            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(port).sync(); // (7)

            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
