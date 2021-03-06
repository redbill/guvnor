/*
 * Copyright 2011 JBoss Inc
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.drools.guvnor.client.explorer.navigation.browse;

import com.google.gwt.user.client.ui.IsTreeItem;
import org.drools.guvnor.client.configurations.Capability;
import org.drools.guvnor.client.explorer.ExplorerNodeConfig;
import org.drools.guvnor.client.explorer.FindPlace;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class BrowseTreeTest extends BrowseTreeTestBase {

    @Override
    @Before
    public void setUp() throws Exception {
        setUpCapabilities();
        super.setUp();
    }

    @Test
    public void testPresenterSet() throws Exception {
        verify( view ).setPresenter( presenter );
    }

    @Test
    public void testFindSelection() throws Exception {
        presenter.onTreeItemSelection( find, "find" );

        verify( placeController ).goTo( any( FindPlace.class ) );
    }

    @Test
    public void testInboxIncomingSelection() throws Exception {
        presenter.onTreeItemSelection( incomingInboxTreeItem, "title1" );

        verify( tabManager ).openInboxIncomingPagedTable( ExplorerNodeConfig.INCOMING_ID );
    }

    @Test
    public void testInboxRecentEditedSelection() throws Exception {
        presenter.onTreeItemSelection( inboxRecentEdited, "title2" );

        verify( tabManager ).openInboxPagedTable( ExplorerNodeConfig.RECENT_EDITED_ID );
    }

    @Test
    public void testInboxRecentViewedSelection() throws Exception {
        presenter.onTreeItemSelection( inboxRecentViewed, "title3" );

        verify( tabManager ).openInboxPagedTable( ExplorerNodeConfig.RECENT_VIEWED_ID );
    }

    @Test
    public void testOpenStateSelection() throws Exception {
        setUpStates( "title1" );
        categoryServiceAsyncMock.addCategorySelection( "/" );
        setUpCapabilities( Capability.SHOW_KNOWLEDGE_BASES_VIEW );

        IsTreeItem state = mock( IsTreeItem.class );
        when( view.addStateItem( "title1" ) ).thenReturn( state );

        setUpPresenter();

        presenter.onTreeItemOpen( rootTreeItem );
        presenter.onTreeItemSelection( state, "title1" );

        verify( tabManager ).openStatePagedTable( "title1" );
    }

    @Test
    public void testLoadRoot() throws Exception {
        verify( view ).addRootTreeItem();
        verify( view ).addFind();
        verify( view, never() ).addRootStateTreeItem();
        verify( view ).addRootCategoryTreeItem();
    }

    @Test
    public void testShowRulesMenuHasCapability() throws Exception {

        setUpCapabilities( Capability.SHOW_CREATE_NEW_ASSET );

        presenter = new BrowseTree( clientFactory );

        verify( view ).showMenu( any( RulesNewMenu.class ) );
    }

    @Test
    public void testShowRulesMenuNoCapability() throws Exception {
        verify( view, never() ).showMenu( any( RulesNewMenu.class ) );
    }

    @Test
    public void testCategorySelection() throws Exception {
        categoryServiceAsyncMock.addCategorySelection( "/", "categoryName1", "categoryName2", "categoryName3" );

        IsTreeItem category1 = mock( IsTreeItem.class );
        when( view.addTreeItem( rootCategoryTreeItem, "categoryName1" ) ).thenReturn( category1 );

        presenter.onTreeItemOpen( rootTreeItem );
        presenter.onTreeItemOpen( rootCategoryTreeItem );
        presenter.onTreeItemSelection( category1, "categoryName1" );

        verify( tabManager ).openCategory( "categoryName1", "/categoryName1" );
    }

    @Test
    public void testLoadFirstLevelCategories() throws Exception {
        categoryServiceAsyncMock.addCategorySelection( "/", "category1", "category2", "category3" );

        presenter.onTreeItemOpen( rootTreeItem );
        presenter.onTreeItemOpen( rootCategoryTreeItem );

        verify( view ).removeCategories( rootCategoryTreeItem );
        verifyAddedTreeItemsToCategory( rootCategoryTreeItem, "category1", "category2", "category3" );
    }

    @Test
    public void testLoadThirdLevelCategory() throws Exception {
        categoryServiceAsyncMock.addCategorySelection( "/", "one", "two", "three" );
        categoryServiceAsyncMock.addCategorySelection( "/one", "t1", "t2" );
        categoryServiceAsyncMock.addCategorySelection( "/one/t2", "a", "b", "c", "d", "e", "f" );

        IsTreeItem oneTreeItem = mock( IsTreeItem.class );
        when( view.addTreeItem( rootCategoryTreeItem, "one" ) ).thenReturn( oneTreeItem );
        IsTreeItem t2TreeItem = mock( IsTreeItem.class );
        when( view.addTreeItem( oneTreeItem, "t2" ) ).thenReturn( t2TreeItem );

        setUpChildren( rootCategoryTreeItem, oneTreeItem );
        setUpChildren( oneTreeItem, t2TreeItem );
        setUpChildren( t2TreeItem );

        presenter.onTreeItemOpen( rootTreeItem );
        presenter.onTreeItemOpen( rootCategoryTreeItem );
        presenter.onTreeItemOpen( oneTreeItem );
        presenter.onTreeItemOpen( t2TreeItem );

        verify( view ).removeCategories( t2TreeItem );
        verifyAddedTreeItemsToCategory( t2TreeItem, "a", "b", "c", "d", "e", "f" );
    }
}
