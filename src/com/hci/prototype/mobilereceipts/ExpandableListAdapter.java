package com.hci.prototype.mobilereceipts;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

	private final Context _context;
	private final List<String> _listDataHeader; // header titles
	// child data in format of header title, child title
	private final Map<String, List<String>> _listDataChild;

	public ExpandableListAdapter(final Context context, final List<String> listDataHeader,
			final Map<String, List<String>> listChildData) {
		_context = context;
		_listDataHeader = listDataHeader;
		_listDataChild = listChildData;
	}

	@Override
	public Object getChild(final int groupPosition, final int childPosititon) {
		return _listDataChild.get(_listDataHeader.get(groupPosition))
				.get(childPosititon);
	}

	@Override
	public long getChildId(final int groupPosition, final int childPosition) {
		return childPosition;
	}

	@Override
	public int getChildrenCount(final int groupPosition) {
		if (_listDataChild.get(_listDataHeader.get(groupPosition)) != null)
			return _listDataChild.get(_listDataHeader.get(groupPosition)).size();
		else 
			return 0;
	}

	@Override
	public View getChildView(final int groupPosition, final int childPosition,
			final boolean isLastChild, View convertView, final ViewGroup parent) {

		final String childText = (String) getChild(groupPosition, childPosition);

		if (convertView == null) {
			final LayoutInflater infalInflater = (LayoutInflater) _context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflater.inflate(R.layout.drawer_list_item, null);
		}

		final TextView txtListChild = (TextView) convertView
				.findViewById(R.id.item);

		txtListChild.setText(childText);
		return convertView;
	}

	@Override
	public Object getGroup(final int groupPosition) {
		return _listDataHeader.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return _listDataHeader.size();
	}

	@Override
	public long getGroupId(final int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(final int groupPosition, final boolean isExpanded,
			View convertView, final ViewGroup parent) {
		final String headerTitle = (String) getGroup(groupPosition);
		if (convertView == null) {
			final LayoutInflater infalInflater = (LayoutInflater) _context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflater.inflate(R.layout.drawer_list_group, null);
		}

		final TextView lblListHeader = (TextView) convertView
				.findViewById(R.id.header);
		lblListHeader.setTypeface(null, Typeface.BOLD);
		lblListHeader.setText(headerTitle);

		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(final int groupPosition, final int childPosition) {
		return true;
	}
}