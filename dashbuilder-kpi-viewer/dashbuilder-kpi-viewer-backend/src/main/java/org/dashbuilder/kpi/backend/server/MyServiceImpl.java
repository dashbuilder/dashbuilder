package org.dashbuilder.kpi.backend.server;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.errai.bus.server.annotations.Service;
import org.dashbuilder.kpi.model.MyModel;
import org.dashbuilder.kpi.service.MyService;

@Service
@ApplicationScoped
public class MyServiceImpl implements MyService {

    @Override
    public MyModel execute( String param ) {
        return new MyModel( "Value from server! " + param );
    }
}
