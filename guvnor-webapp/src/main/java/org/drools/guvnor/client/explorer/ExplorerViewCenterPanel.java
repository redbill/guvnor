/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.guvnor.client.explorer;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.*;
import org.drools.guvnor.client.common.LoadingPopup;
import org.drools.guvnor.client.packages.PackageEditorWrapper;
import org.drools.guvnor.client.util.ScrollTabLayoutPanel;
import org.drools.guvnor.client.util.TabOpenerImpl;
import org.drools.guvnor.client.util.TabbedPanel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is the tab panel manager.
 */
public class ExplorerViewCenterPanel extends Composite implements TabbedPanel {

    private final ScrollTabLayoutPanel tabLayoutPanel;

    private PanelMap openedTabs = new PanelMap();

    private Map<String, PackageEditorWrapper> openedPackageEditors = new HashMap<String, PackageEditorWrapper>();

    private ClientFactory clientFactory;

    public ExplorerViewCenterPanel( ClientFactory clientFactory ) {
        this.clientFactory = clientFactory;
        tabLayoutPanel = new ScrollTabLayoutPanel( 2,
                Unit.EM );
        initWidget( tabLayoutPanel );

        TabContainer.init( new TabOpenerImpl( clientFactory, this ) {
        } );
    }

    public boolean contains( String key ) {
        return openedTabs.contains( key );
    }

    public void show( String key ) {
        if ( openedTabs.contains( key ) ) {
            LoadingPopup.close();
            tabLayoutPanel.selectTab( openedTabs.get( key ) );
        }
    }

    /**
     * Add a new tab. Should only do this if have checked showIfOpen to avoid dupes being opened.
     *
     * @param tabname The displayed tab name.
     * @param widget  The contents.
     * @param key     A key which is unique.
     */
    public void addTab( final String tabname,
                        IsWidget widget,
                        final String key ) {

        ScrollPanel localTP = new ScrollPanel();
        localTP.add( widget );
        tabLayoutPanel.add( localTP,
                newClosableLabel(
                        tabname,
                        key
                ) );
        tabLayoutPanel.selectTab( localTP );

        if ( widget instanceof PackageEditorWrapper ) {
            this.getOpenedPackageEditors().put( tabname,
                    (PackageEditorWrapper) widget );
        }

        openedTabs.put( key,
                localTP );
    }

    private Widget newClosableLabel( final String title,
                                     final String panelId ) {
        ClosableLabel closableLabel = new ClosableLabel( title );

        closableLabel.addCloseHandler( new CloseHandler<ClosableLabel>() {
            public void onClose( CloseEvent<ClosableLabel> event ) {
                close( panelId );
            }

        } );

        return closableLabel;
    }

    /**
     * Will open if existing. If not it will return false;
     */
    public boolean showIfOpen( String key ) {
        if ( openedTabs.contains( key ) ) {
            LoadingPopup.close();
            tabLayoutPanel.selectTab( openedTabs.get( key ) );
            return true;
        }
        return false;
    }

    public void close( String key ) {

        int widgetIndex = openedTabs.getIndex( key );

        Place nextPlace = getPlace( widgetIndex );

        tabLayoutPanel.remove( openedTabs.get( key ) );
        openedTabs.remove( key );

        if ( nextPlace != null ) {
            goTo( nextPlace );
        }
    }

    private Place getPlace( int widgetIndex ) {
        if ( isOnlyOneTabLeft() ) {
            return Place.NOWHERE;
        } else if ( isSelectedTabIndex( widgetIndex ) ) {
            return getNeighbour( widgetIndex );
        } else {
            return null;
        }
    }

    private void goTo( Place place ) {
        clientFactory.getPlaceController().goTo( place );
    }

    private Place getNeighbour( int widgetIndex ) {
        if ( isLeftMost( widgetIndex ) ) {
            return getNextPlace();
        } else {
            return getPreviousPlace();
        }
    }

    private boolean isLeftMost( int widgetIndex ) {
        return widgetIndex == 0;
    }

    private boolean isSelectedTabIndex( int widgetIndex ) {
        return tabLayoutPanel.getSelectedIndex() == widgetIndex;
    }

    private Place getPreviousPlace() {
        return clientFactory.getPlaceHistoryMapper().getPlace(
                openedTabs.getKey( tabLayoutPanel.getSelectedIndex() - 1 ) );
    }

    private Place getNextPlace() {
        return clientFactory.getPlaceHistoryMapper().getPlace(
                openedTabs.getKey( tabLayoutPanel.getSelectedIndex() + 1 ) );
    }

    private boolean isOnlyOneTabLeft() {
        return tabLayoutPanel.getWidgetCount() == 1;
    }

    public Map<String, PackageEditorWrapper> getOpenedPackageEditors() {
        return openedPackageEditors;
    }

    private class PanelMap {
        private final Map<String, Panel> keysToPanel = new HashMap<String, Panel>();
        private final List<String> keys = new ArrayList<String>();

        Panel get( String key ) {
            return keysToPanel.get( key );
        }

        String getKey( int index ) {
            return keys.get( index );
        }

        void remove( String key ) {
            keys.remove( key );
            keysToPanel.remove( key );
        }

        public boolean contains( String key ) {
            return keysToPanel.containsKey( key );
        }

        public void put( String key, Panel panel ) {
            keys.add( key );
            keysToPanel.put( key, panel );
        }

        public int getIndex( String key ) {
            return keys.indexOf( key );
        }
    }
}
