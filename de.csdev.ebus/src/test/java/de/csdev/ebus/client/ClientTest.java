package de.csdev.ebus.client;

import static org.junit.Assert.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.csdev.ebus.cfg.EBusConfigurationReaderException;
import de.csdev.ebus.command.IEBusCommandMethod;
import de.csdev.ebus.command.datatypes.EBusTypeException;
import de.csdev.ebus.core.EBusConnectorEventListener;
import de.csdev.ebus.core.EBusController;
import de.csdev.ebus.core.EBusDataException;
import de.csdev.ebus.core.connection.EBusEmulatorConnection;
import de.csdev.ebus.service.parser.EBusParserListener;
import de.csdev.ebus.utils.EBusUtils;
import de.csdev.ebus.wip.EBusStateMachineTest;

public class ClientTest {

    private static final Logger logger = LoggerFactory.getLogger(EBusStateMachineTest.class);

    private EBusEmulatorConnection emulator;

    @Before
    public void before() throws IOException, EBusConfigurationReaderException {
        emulator = new EBusEmulatorConnection(null);
    }

    @Test
    public void xxx() throws EBusTypeException, IOException, InterruptedException {

        EBusClientConfiguration clientConfiguration = new EBusClientConfiguration();

        clientConfiguration.loadInternalConfigurations();

        EBusClient client = new EBusClient(clientConfiguration);

        EBusController controller = new EBusController(emulator);

        client.connect(controller, (byte) 0xFF);

        client.getController().addEBusEventListener(new EBusConnectorEventListener() {

            public void onTelegramReceived(byte[] receivedData, Integer sendQueueId) {
                // noop
            }

            public void onTelegramException(EBusDataException exception, Integer sendQueueId) {
                fail("No TelegramException expected!");
            }

            public void onConnectionException(Exception e) {
                fail("No ConnectionException expected!");
            }
        });

        client.getResolverService().addEBusParserListener(new EBusParserListener() {

            public void onTelegramResolved(IEBusCommandMethod commandChannel, Map<String, Object> result,
                    byte[] receivedData, Integer sendQueueId) {

                assertTrue(result.containsKey("pressure"));
                assertEquals(new BigDecimal("1.52"), result.get("pressure"));
                logger.info("Result correct!");
            }
        });

        controller.start();

        writeTelegramToEmulator("30 08 50 22 03 CC 1A 27 59 00 02 98 00 0C 00");

        // wait a bit for the ebus thread
        Thread.sleep(10);
    }

    private void writeTelegramToEmulator(String telegram) throws IOException {

        emulator.writeByte(0xAA);
        emulator.writeByte(0xAA);
        emulator.writeByte(0xAA);

        byte[] bs = EBusUtils.toByteArray(telegram);

        for (byte b : bs) {
            emulator.writeByte(b);
        }

        emulator.writeByte(0xAA);
        emulator.writeByte(0xAA);
    }

}
