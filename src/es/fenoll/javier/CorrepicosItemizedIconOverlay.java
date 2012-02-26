package es.fenoll.javier;

import java.util.List;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;

import android.content.Context;

public class CorrepicosItemizedIconOverlay<Item extends OverlayItem> extends ItemizedOverlayWithFocus<Item> {

	public CorrepicosItemizedIconOverlay(
			Context pContext,
			List<Item> pList,
			org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener<Item> pOnItemGestureListener) {
		
		super(pContext, pList, pOnItemGestureListener);
		
		this.setFocusItemsOnTap(true);
        this.setFocusedItem(0);

		
	}
    
	public int getFocusedItemIndex() {
        
        return this.mFocusedItemIndex;
}

	
	public void setFocusNext() {
		
		if (this.mFocusedItemIndex == NOT_SET) {
            return;
		}
		
		// si estoy en el último me paso al primero
		if( this.mFocusedItemIndex == super.mItemList.size()-1 ) {
			this.mFocusedItemIndex = 0;
		} 
		else {
			this.mFocusedItemIndex += 1;
		}
			
	}
	public void setFocusPrev() {
		
		if (this.mFocusedItemIndex == NOT_SET) {
            return;
		}
		
		// si estoy en el último me paso al primero
		if( this.mFocusedItemIndex == 0 ) {
			this.mFocusedItemIndex = super.mItemList.size() -1;
		} 
		else {
			this.mFocusedItemIndex -= 1;
		}
		
	}


	public IGeoPoint getFocusedGeoPoint() {

		return getFocusedItem().getPoint();

	}

}

