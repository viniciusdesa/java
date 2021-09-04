import io.netty.buffer.ByteBuf;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

/**
 * Handles a server-side channel.
 * DiscardServerHandler extende ChannelInboundHandlerAdapter, que é uma implementação do ChannelInboundHandler.
 * ChannelInboundHandler fornece vários métodos event handler que vc pode sobrecarregar.
 * Por agora, é o suficiente para extender o ChannelInboundHandlerAdapter do que implementar a interface handler vc mesmo.
 */
public class EchoServerHandler extends ChannelInboundHandlerAdapter { // (1)

    /**
     * Chamado com a mensagem recebida, sempre que novos dados forem recebidos, a partir de um client.
     * Neste exemplo, o tipo de mensagem recebida é ByteBuf.
     * @param ctx
     * @param msg
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) { // (2)
        ctx.write(msg); // (1)
        ctx.flush(); // (2)
    }

    /**
     * O event handler exceptionCaught() é chamado com um Throwable quando uma exceção é disparada pelo Netty
     * devido a um erro de I/O ou por uma implementação do handler devido à exceção disparada enquanto processando eventos.
     * Na maioria dos casos, a exceção obtida deve ser logada no seu canal associado deve ser fechada aqui, embora a implementação
     * deste método possa ser diferente dependendo do que você quer lidar com uma situação excepcional. Por exemplo, você pode querer
     * enviar uma mensagem de resposta com um código de erro antes de fechar a conexão.
     * @param ctx
     * @param cause
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }
}