/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS.
 *
 * Copyright (C) 2015 Chair for Network and Data Security,
 *                    Ruhr University Bochum
 *                    (juraj.somorovsky@rub.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.rub.nds.tlsattacker.eap;

/**
 * State for Identity. Sends the Identity Frame. Change state if a Client Hello,
 * EAP-TLS Start or Failure Frame was received.
 * 
 * @author Felix Lange <flx.lange@gmail.com>
 */
public class IdentityState implements EapState {

    EapolMachine eapolMachine;

    int id;

    EapFactory eaptlsfactory = new EapTlsFactory();

    NetworkHandler nic = NetworkHandler.getInstance();

    byte[] data = {};

    public IdentityState(EapolMachine eapolMachine, int id) {

	this.eapolMachine = eapolMachine;
	this.id = id;
    }

    @Override
    public void send() {

	EAPFrame eapstart = eaptlsfactory.createFrame("EAPID", id);
	nic.sendFrame(eapstart.getFrame());

    }

    @Override
    public void sendTLS(byte[] tlspacket) {

    }

    @Override
    public byte[] receive() {

	data = nic.receiveFrame();
	id = (int) data[19]; // Get ID

	if (data[22] == 0x0d) {
	    eapolMachine.setState(new HelloState(eapolMachine, id));
	} else if (data[18] == 0x04) {
	    eapolMachine.setState(new FailureState(eapolMachine, id));
	} else {
	    eapolMachine.setState(new EapTlsStartState(eapolMachine, id));
	}
	return data;

    }

    @Override
    public String getState() {
	return "IdentityState";
    }

    @Override
    public int getID() {

	return id;

    }

}
