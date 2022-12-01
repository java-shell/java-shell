package terra.shell.emulation.concurrency.math.cluster.data;

import java.io.Serializable;
import java.net.InetAddress;

public record BasicImmutableData<T> (InetAddress ip, T data) implements Serializable {

}