/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dashbuilder.dataprovider.backend.elasticsearch.rest.impl;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import java.net.InetAddress;

public class NativeClientFactory {

    private static final NativeClientFactory INSTANCE = new NativeClientFactory();
    
    protected static final String EL_CLUTER_NAME = "cluster.name";
    protected static final String EL_CLIENT_TIMEOUT = "client.transport.ping_timeout";

    private Client testClient;
    
    public static NativeClientFactory getInstance() {
        return INSTANCE;
    }
    
    public void setTestClient( Client c ) {
        this.testClient = c;
    }

    public Client newClient( String serverURL, String clusterName , long timeout ) throws Exception {

        if ( null != testClient ) {
            
            return testClient;
            
        }

        if ( null == clusterName || clusterName.trim().length() == 0 ) {
            throw new IllegalArgumentException("Parameter clusterName is missing.");
        }

        if ( null == serverURL || serverURL.trim().length() == 0 ) {
            throw new IllegalArgumentException("Parameter serverURL is missing.");
        }

        String[] url = serverURL.split( ":" );
        if ( url.length != 2 ) {
            throw new IllegalArgumentException("Invalid serverURL format. Expected format <HOST>:<PORT>");
        }

        String t = ( timeout / 1000 ) + "s";

        Settings settings = Settings.settingsBuilder()
                .put(EL_CLUTER_NAME, clusterName)
                .put(EL_CLIENT_TIMEOUT, t)
                .build();

        return TransportClient.builder().settings(settings).build()
                .addTransportAddress(new InetSocketTransportAddress(
                        InetAddress.getByName( url[0]), Integer.parseInt( url[1] ) ));

    }
    
}
