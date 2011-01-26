package de.bitzeche.logback.scribe;

/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TTransportException;

import com.facebook.infrastructure.service.LogEntry;
import com.facebook.infrastructure.service.scribe.Client;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;

import java.util.List;
import java.util.ArrayList;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.Socket;
import java.io.IOException;

public class ScribeAppender extends AppenderBase<ILoggingEvent> {
	
	private List<LogEntry> logEntries;
	private String hostname;
	private String scribeHost = "127.0.0.1";
	private int scribePort = 1463;
	private String scribeCategory = "scribe";

	private Client client;
	private TFramedTransport transport;

	private Layout<ILoggingEvent> layout;

	public void start() {
		int errorCount = 0;
		try {
			synchronized (this) {
				if (hostname == null) {
					try {
						hostname = InetAddress.getLocalHost()
								.getCanonicalHostName();
					} catch (UnknownHostException e) {
						// can't get hostname
						addError("The Hostanme option is mandatory");
						errorCount++;
					}
				}

				// Thrift boilerplate code
				logEntries = new ArrayList<LogEntry>(1);
				TSocket sock = new TSocket(new Socket(scribeHost, scribePort));
				transport = new TFramedTransport(sock);
				TBinaryProtocol protocol = new TBinaryProtocol(transport,
						false, false);
				client = new Client(protocol, protocol);

				if (errorCount == 0) {
					super.start();
				}
			}
		} catch (TTransportException e) {
			System.err.println(e);
		} catch (UnknownHostException e) {
			System.err.println(e);
		} catch (IOException e) {
			System.err.println(e);
		} catch (Exception e) {
			System.err.println(e);
		}
	}

	public void stop() {
		if (transport != null && transport.isOpen()) {
			transport.close();
		}
		super.stop();
	}

	/*
	 * Appends a log message to Scribe
	 */
	@Override
	public void append(ILoggingEvent loggingEvent) {
		if (!isStarted()) {
			return;
		}

		try {
			String msg = layout.doLayout(loggingEvent);
			LogEntry entry = new LogEntry(scribeCategory, msg, null);
			logEntries.add(entry);
			client.Log(logEntries);
		} catch (TTransportException e) {
			transport.close();
		} catch (Exception e) {
			System.err.println(e);
		} finally {
			logEntries.clear();
		}
	}

	public void setLayout(Layout<ILoggingEvent> layout) {
		this.layout = layout;
	}
	
	public String getScribeHost() {
		return scribeHost;
	}

	public void setScribeHost(String scribeHost) {
		this.scribeHost = scribeHost;
	}

	public int getScribePort() {
		return scribePort;
	}

	public void setScribePort(int scribePort) {
		this.scribePort = scribePort;
	}

	public String getScribeCategory() {
		return scribeCategory;
	}

	public void setScribeCategory(String scribeCategory) {
		this.scribeCategory = scribeCategory;
	}

	public TFramedTransport getTransport() {
		return transport;
	}

	public void setTransport(TFramedTransport transport) {
		this.transport = transport;
	}

	public Layout<ILoggingEvent> getLayout() {
		return layout;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
}