package org.dashbuilder.kpi.client;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import org.dashbuilder.kpi.model.MyModel;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.dashbuilder.kpi.service.MyService;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;

@WorkbenchScreen(identifier = "KPIPresenter")
@Dependent
public class KPIPresenter {

    public interface View extends IsWidget {

        void setValue( String value );
    }

    @Inject
    private Caller<MyService> myService;

    @Inject
    private View view;

    @PostConstruct
    private void init() {
        myService.call( new RemoteCallback<MyModel>() {
            @Override
            public void callback( MyModel response ) {
                view.setValue( response.getValue() );
            }
        } ).execute( "hi" );
    }

    @WorkbenchPartTitle
    public String getTitle() {
        return "KPI View";
    }

    @WorkbenchPartView
    public IsWidget getView() {
        return view;
    }

}
