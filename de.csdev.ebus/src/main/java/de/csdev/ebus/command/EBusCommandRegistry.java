/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package de.csdev.ebus.command;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * @author Christian Sowada
 *
 */
public class EBusCommandRegistry {

    private List<IEBusCommand> list = new ArrayList<IEBusCommand>();

    public void addTelegramConfigurationList(List<EBusCommand> nlist) {
        list.addAll(nlist);
    }

    public void addTelegramConfiguration(IEBusCommand telegramCfg) {
        list.add(telegramCfg);
    }

    public List<IEBusCommand> find(byte[] data) {
        return find(ByteBuffer.wrap(data));
    }

    public List<IEBusCommand> getConfigurationList() {
        return Collections.unmodifiableList(list);
    }

    public IEBusCommand getConfigurationById(String id) {
    	
    	for (IEBusCommand command : list) {
    		if(StringUtils.equals(command.getId(), id))
				return command;
		}
    	
        return null;
    }
    
    public List<IEBusCommand> find(ByteBuffer data) {

        ArrayList<IEBusCommand> result = new ArrayList<IEBusCommand>();

        for (IEBusCommand telegramCfg : list) {

            ByteBuffer masterTelegram = EBusCommandUtils.buildMasterTelegram(telegramCfg, (byte) 0x00, (byte) 0xFF, null);
            ByteBuffer mask = telegramCfg.getMasterTelegramMask();

            for (int i = 0; i < mask.position(); i++) {
                byte b = mask.get(i);

                if (b == (byte) 0xFF) {
                    if (masterTelegram.get(i) != data.get(i)) {
                        break;
                    }
                }
                if (i == mask.position() - 1) {
                    result.add(telegramCfg);
                }
            }
        }

        return result;

    }

}
