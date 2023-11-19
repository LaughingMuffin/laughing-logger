/*
 * Copyright (C) 2006 The Android Open Source Project
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
 * 
 * stolen almost completely from ArrayAdapter.java - nolan
 */

package org.laughing.logger.data;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.laughing.logger.R;
import org.laughing.logger.helper.PreferenceHelper;
import org.laughing.logger.util.LogLineAdapterUtil;
import org.laughing.logger.util.StopWatch;
import org.laughing.logger.util.UtilLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LogLineAdapter extends RecyclerView.Adapter<LogLineViewHolder> implements Filterable {

    private static UtilLogger log = new UtilLogger(LogLineAdapter.class);
    /**
     * Lock used to modify the content of {@link #mObjects}. Any write operation
     * performed on the array should be synchronized on this lock. This lock is also
     * used by the filter (see {@link #getFilter()} to make a synchronized copy of
     * the original array of data.
     */
    private final Object mLock = new Object();
    private Comparator<? super LogLine> mComparator;
    /**
     * Contains the list of objects that represent the data of this ArrayAdapter.
     * The content of this list is referred to as "the array" in the documentation.
     */
    private List<LogLine> mObjects;

    private LogLineViewHolder.OnClickListener mClickListener;

    private ArrayList<LogLine> mOriginalValues;
    private ArrayFilter mFilter;

    private int logLevelLimit = 0;


    /**
     * Constructor
     */
    public LogLineAdapter() {
        mObjects = new ArrayList<>();

        setHasStableIds(true);
    }


    /**
     * Adds the specified object at the end of the array.
     *
     * @param object The object to add at the end of the array.
     */
    public void add(LogLine object, boolean notify) {
        if (mOriginalValues != null) {
            synchronized (mLock) {
                mOriginalValues.add(object);
                mObjects.add(object);

            }
        } else {
            mObjects.add(object);
        }
        if (notify) {
            notifyItemInserted(mObjects.size());
        }
    }


    public void addWithFilter(LogLine object, CharSequence text, boolean notify) {

        if (mOriginalValues != null) {

            List<LogLine> inputList = Collections.singletonList(object);

            if (mFilter == null) {
                mFilter = new ArrayFilter();
            }

            List<LogLine> filteredObjects = mFilter.performFilteringOnList(inputList, text);

            synchronized (mLock) {
                mOriginalValues.add(object);

                mObjects.addAll(filteredObjects);
                if (notify) {
                    notifyItemRangeInserted(mObjects.size() - filteredObjects.size(), filteredObjects.size());
                }
            }
        } else {
            synchronized (mLock) {
                mObjects.add(object);
            }
            if (notify) {
                notifyItemInserted(mObjects.size());
            }
        }


    }

    /**
     * Inserts the specified object at the specified index in the array.
     *
     * @param object The object to insert into the array.
     * @param index  The index at which the object must be inserted.
     */
    public void insert(LogLine object, int index) {
        if (mOriginalValues != null) {
            synchronized (mLock) {
                mOriginalValues.add(index, object);
                notifyDataSetChanged();
            }
        } else {
            mObjects.add(index, object);
            notifyDataSetChanged();
        }
    }

    /**
     * Removes the specified object from the array.
     *
     * @param object The object to remove.
     */
    public void remove(LogLine object) {
        if (mOriginalValues != null) {
            synchronized (mLock) {
                mOriginalValues.remove(object);
            }
        } else {
            mObjects.remove(object);
        }
        notifyDataSetChanged();
    }

    public void removeFirst(int n) {
        StopWatch stopWatch = new StopWatch("removeFirst()");
        if (mOriginalValues != null) {
            synchronized (mLock) {
                List<LogLine> subList = mOriginalValues.subList(n, mOriginalValues.size());
                for (int i = 0; i < n; i++) {
                    // value to delete - delete it from the mObjects as well
                    mObjects.remove(mOriginalValues.get(i));
                }
                mOriginalValues = new ArrayList<>(subList);
            }
        } else {
            synchronized (mLock) {
                mObjects = new ArrayList<>(mObjects.subList(n, mObjects.size()));
            }
        }
        notifyDataSetChanged();
        stopWatch.log(log);
    }

    /**
     * Remove all elements from the list.
     */
    public void clear() {
        if (mOriginalValues != null) {
            synchronized (mLock) {
                mOriginalValues.clear();
                mObjects.clear();
            }
        } else {
            mObjects.clear();
        }
        notifyDataSetChanged();
    }

    /**
     * Sorts the content of this adapter using the specified comparator.
     *
     * @param comparator The comparator used to sort the objects contained
     *                   in this adapter.
     */
    public void sort(Comparator<? super LogLine> comparator) {
        this.mComparator = comparator;
        Collections.sort(mObjects, comparator);
        notifyDataSetChanged();
    }

    public LogLine getItem(int position) {
        return mObjects.get(position);
    }

    public List<LogLine> getTrueValues() {
        return mOriginalValues != null ? mOriginalValues : mObjects;
    }

    @Override
    public LogLineViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_logcat, parent, false);
        return new LogLineViewHolder(v, mClickListener);
    }

    @Override
    public void onBindViewHolder(LogLineViewHolder holder, int position) {
        Context context = holder.itemView.getContext();

        LogLine logLine;
        try {
            logLine = getItem(position);
        } catch (IndexOutOfBoundsException ignore) {
            // XXX hack - I sometimes get array index out of bounds exceptions here
            // no idea how to solve it, so this is the best I can do
            //TODO: Fix
            logLine = LogLine.newLogLine(
                    "",
                    PreferenceHelper.getExpandedByDefaultPreference(context),
                    PreferenceHelper.getFilterPatternPreference(context)
            );
        }

        holder.logLine = logLine;

        TextView t = holder.itemView.findViewById(R.id.log_level_text);
        t.setText(logLine.getProcessIdText());
        t.setBackgroundColor(LogLineAdapterUtil.getBackgroundColorForLogLevel(context, logLine.getLogLevel()));
        t.setTextColor(LogLineAdapterUtil.getForegroundColorForLogLevel(context, logLine.getLogLevel()));
        t.setVisibility(logLine.getLogLevel() == -1 ? View.GONE : View.VISIBLE);

        int textColor = PreferenceHelper.getColorScheme(context).getForegroundColor(context);
        float textSize = PreferenceHelper.getTextSizePreference(context);

        //OUTPUT TEXT VIEW
        TextView output = holder.itemView.findViewById(R.id.log_output_text);
        output.setSingleLine(!logLine.isExpanded());
        output.setText(logLine.getLogOutput());
        output.setTextColor(textColor);


        //TAG TEXT VIEW
        TextView tag = holder.itemView.findViewById(R.id.tag_text);
        tag.setSingleLine(!logLine.isExpanded());
        tag.setText(logLine.getTag());
        tag.setVisibility(logLine.getLogLevel() == -1 ? View.GONE : View.VISIBLE);


        //TEXT SIZE
        tag.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        output.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);

        //EXPANDED INFO
        boolean extraInfoIsVisible = logLine.isExpanded()
                && PreferenceHelper.getShowTimestampAndPidPreference(context)
                && logLine.getProcessId() != -1; // -1 marks lines like 'beginning of /dev/log...'

        TextView pidText = holder.itemView.findViewById(R.id.pid_text);
        pidText.setVisibility(extraInfoIsVisible ? View.VISIBLE : View.GONE);
        TextView timestampText = holder.itemView.findViewById(R.id.timestamp_text);
        timestampText.setVisibility(extraInfoIsVisible ? View.VISIBLE : View.GONE);

        if (extraInfoIsVisible) {

            pidText.setTextColor(textColor);
            timestampText.setTextColor(textColor);

            pidText.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
            timestampText.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);

            pidText.setText(logLine.getProcessId() != -1 ? Integer.toString(logLine.getProcessId()) : null);
            timestampText.setText(logLine.getTimestamp());
        }

        tag.setTextColor(LogLineAdapterUtil.getOrCreateTagColor(context, logLine.getTag()));

        // if this is a "partially selected" log, change the color to orange or whatever

        int selectedBackground = logLine.isHighlighted()
                ? PreferenceHelper.getColorScheme(context).getSelectedColor(context)
                : ContextCompat.getColor(context,android.R.color.transparent);
        holder.itemView.setBackgroundColor(selectedBackground);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getItemId(int position) {
        synchronized (mLock) {
            return mObjects.get(position).getOriginalLine().hashCode();
        }
    }

    @Override
    public int getItemCount() {
        synchronized (mLock) {
            return mObjects.size();
        }
    }

    public int getLogLevelLimit() {
        return logLevelLimit;
    }


    public void setLogLevelLimit(int logLevelLimit) {
        this.logLevelLimit = logLevelLimit;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new ArrayFilter();
        }
        return mFilter;
    }

    public List<LogLine> getObjects() {
        return mObjects;
    }

    public void setClickListener(LogLineViewHolder.OnClickListener clickListener) {
        mClickListener = clickListener;
    }

    /**
     * <p>An array filter constrains the content of the array adapter with
     * a prefix. Each item that does not start with the supplied prefix
     * is removed from the list.</p>
     */
    private class ArrayFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();

            if (mOriginalValues == null) {
                synchronized (mLock) {
                    mOriginalValues = new ArrayList<>(mObjects);
                }
            }

            ArrayList<LogLine> allValues = performFilteringOnList(mOriginalValues, prefix);

            results.values = allValues;
            results.count = allValues.size();

            return results;
        }

        public ArrayList<LogLine> performFilteringOnList(List<LogLine> inputList, CharSequence query) {

            SearchCriteria searchCriteria = new SearchCriteria(query);

            // search by log level
            ArrayList<LogLine> allValues = new ArrayList<>();

            ArrayList<LogLine> logLines;
            synchronized (mLock) {
                logLines = new ArrayList<>(inputList);
            }

            for (LogLine logLine : logLines) {
                if (logLine != null &&
                        LogLineAdapterUtil.logLevelIsAcceptableGivenLogLevelLimit(logLine.getLogLevel(), logLevelLimit)) {
                    allValues.add(logLine);
                }
            }
            ArrayList<LogLine> finalValues = allValues;

            // search by criteria
            if (!searchCriteria.isEmpty()) {

                final ArrayList<LogLine> values = allValues;
                final int count = values.size();

                final ArrayList<LogLine> newValues = new ArrayList<>(count);

                for (int i = 0; i < count; i++) {
                    final LogLine value = values.get(i);
                    // search the logline based on the criteria
                    if (searchCriteria.matches(value)) {
                        newValues.add(value);
                    }
                }

                finalValues = newValues;
            }

            // sort here to ensure that filtering the list doesn't mess up the sorting
            if (mComparator != null) {
                Collections.sort(finalValues, mComparator);
            }

            return finalValues;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            //noinspection unchecked

            //log.d("filtering: %s", constraint);


            mObjects = (List<LogLine>) results.values;
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetChanged();
            }
        }
    }

}
