package com.example.websocket_demo.service.network.impl;

import com.example.websocket_demo.dto.request.WolRequest;
import com.example.websocket_demo.service.network.WolService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

@Slf4j
@Service
public class WolServiceImpl implements WolService {

    @Override
    public void wakeOnLan(String macAddress, String host, int port) {
        try {
            byte[] macBytes = getMacBytes(macAddress);
            byte[] bytes = new byte[6 + 16 * macBytes.length];

            // 6 bytes of 0xFF
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) 0xff;
            }

            // 16 repetitions of MAC address
            for (int i = 6; i < bytes.length; i += macBytes.length) {
                System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
            }

            InetAddress address = InetAddress.getByName(host);
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, port);

            try (DatagramSocket socket = new DatagramSocket()) {
                socket.setBroadcast(true);
                socket.send(packet);
                log.info("Wake-on-LAN packet sent to MAC: {}, Host: {}, Port: {}", 
                        macAddress, host, port);
            }

        } catch (Exception e) {
            log.error("Failed to send Wake-on-LAN packet to MAC: {}", macAddress, e);
            throw new RuntimeException("Failed to send Wake-on-LAN packet: " + e.getMessage(), e);
        }
    }

    private byte[] getMacBytes(String macStr) throws IllegalArgumentException {
        byte[] bytes = new byte[6];
        String[] hex = macStr.split("(:|-)");
        if (hex.length != 6) {
            throw new IllegalArgumentException("Invalid MAC address.");
        }
        try {
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) Integer.parseInt(hex[i], 16);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid hex digit in MAC address.");
        }
        return bytes;
    }
}
