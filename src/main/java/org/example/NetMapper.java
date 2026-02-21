package org.example;

import org.pcap4j.core.*;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.Packet;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class NetMapper {

    private Map<InetAddress, String> ipMap = new ConcurrentHashMap<>();
    private PcapHandle handle;
    private ExecutorService exec;
    private AtomicInteger counter = new AtomicInteger(0);

    public String mapNetwork() {
        try {
            Stream<NetworkInterface> stream = NetworkInterface.networkInterfaces();
            return stream.filter(ni -> {
                        try {
                            return ni.isUp() && !ni.isLoopback() && !ni.isVirtual();
                        } catch (SocketException e) {
                            return false;
                        }
                    })
                    .filter( ni ->{
                        String dn = ni.getDisplayName();
                        return !(dn.contains("VMware") || dn.contains("Virtual") || dn.contains("Radmin") || dn.contains("VPN"));
                    })
                    .flatMap(NetworkInterface::inetAddresses)
                    .filter(ia ->  ia instanceof Inet4Address && ia.isSiteLocalAddress())
                    .map(InetAddress::getHostAddress)
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void captureNetwork(Queue<PacketInfo> packetQueue) {
        String ip = mapNetwork();
        counter.set(0);
        try{
            InetAddress addr = InetAddress.getByName(ip);
            PcapNetworkInterface nif = Pcaps.getDevByAddress(addr);

            int snapLen = 65536;
            int timeout = 10;

            PcapNetworkInterface.PromiscuousMode mode = PcapNetworkInterface.PromiscuousMode.PROMISCUOUS;
            handle = nif.openLive(snapLen, mode, timeout);

            PacketListener listener = packet -> {
                if (packet.contains(IpV4Packet.class)) {
                    IpV4Packet ipV4Packet = packet.get(IpV4Packet.class);
                    IpV4Packet.IpV4Header srcAddrHeader = ipV4Packet.getHeader();
                    Packet payload = ipV4Packet.getPayload();
                    int length = ipV4Packet.length();

                    String source = getHostNameByCache(srcAddrHeader.getDstAddr());
                    String destination = getHostNameByCache(srcAddrHeader.getSrcAddr());
                    String protocol = ipV4Packet.getHeader().getProtocol().toString();


                    PacketInfo packetInfo = new PacketInfo(
                            counter.getAndIncrement(),
                            source,
                            destination,
                            protocol,
                            length,
                            ipV4Packet.getRawData(),
                            Instant.now().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss"))
                    );

                    packetQueue.add(packetInfo);
                }
            };

            exec = Executors.newSingleThreadExecutor();

            exec.execute(() -> {
                try{
                    handle.loop(-1, listener);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopCaptureNetwork() {
        try{
            handle.breakLoop();
            handle.close();
            exec.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String getHostNameByCache(InetAddress add) {
        return ipMap.computeIfAbsent(add, InetAddress::getHostName);
    }
}
