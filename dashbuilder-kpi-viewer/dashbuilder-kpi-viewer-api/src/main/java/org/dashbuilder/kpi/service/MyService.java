package org.dashbuilder.kpi.service;

import org.jboss.errai.bus.server.annotations.Remote;
import org.dashbuilder.kpi.model.MyModel;

@Remote
public interface MyService {

    public MyModel execute( final String param );

}
