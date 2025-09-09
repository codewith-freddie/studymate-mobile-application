package com.example.studymate.UserDashboard.Room.FacultyActivity.Home;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studymate.DeleteHandler.AnnouncementDeleteHandler;
import com.example.studymate.Dialog.AddReferencesHandler;
import com.example.studymate.DeleteHandler.LinkDeleteHandler;
import com.example.studymate.DeleteHandler.ModuleDeleteHandler;
import com.example.studymate.DeleteHandler.SyllabusDeleteHandler;
import com.example.studymate.Dialog.DisplayReferencesHandler;
import com.example.studymate.Dialog.EditReferencesHandler;
import com.example.studymate.R;

import java.util.Collections;
import java.util.List;

public class HomeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int PDF_INFO_TYPE = 1;
    private static final int ANNOUNCEMENT_TYPE = 2;
    private static final int SYLLABUS_TYPE = 3;
    private static final int LINK_TYPE = 4;


    private List<ListItem> itemList;
    private Context context;
    private String roomId; // Assuming you need roomId for deleting the item

    public HomeAdapter(Context context, List<ListItem> itemList, String roomId) {
        this.context = context;
        this.itemList = itemList;
        this.roomId = roomId;
        sortItemsByDate(); // Sort items initially
    }

    // Add this method to update the data
    public void updateData(List<ListItem> newItemList) {
        this.itemList = newItemList; // Update the internal list
        notifyDataSetChanged(); // Notify the adapter of the new data
    }

    // Sort items by timestamp in ascending order (newest at the bottom)
    private void sortItemsByDate() {
        Collections.sort(itemList, (item1, item2) -> {
            long time1 = getTimestamp(item1);
            long time2 = getTimestamp(item2);
            return Long.compare(time1, time2); // Ascending order (newest at the bottom)
        });
        notifyDataSetChanged();
    }

    public List<ListItem> getItemList() {
        return itemList;
    }

    private long getTimestamp(ListItem item) {
        if (item instanceof PdfInfo) {
            return ((PdfInfo) item).getTimestamp(); // Use timestamp for sorting
        } else if (item instanceof Announcement) {
            return ((Announcement) item).getTimestamp(); // Use timestamp for sorting
        } else if (item instanceof SyllabusInfo) {
            return ((SyllabusInfo) item).getTimestamp(); // Use timestamp for sorting
        }else if (item instanceof LinkInfo) {
            return ((LinkInfo) item).getTimestamp(); // Use timestamp for sorting
        }
        return 0; // Default to 0 if no timestamp is available
    }

    @Override
    public int getItemViewType(int position) {
        return itemList.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == PDF_INFO_TYPE) {
            View view = inflater.inflate(R.layout.item_pdf, parent, false);
            return new PdfInfoViewHolder(view);
        } else if (viewType == ANNOUNCEMENT_TYPE) {
            View view = inflater.inflate(R.layout.item_announcement, parent, false);
            return new AnnouncementViewHolder(view);
        } else if (viewType == SYLLABUS_TYPE) {
            View view = inflater.inflate(R.layout.item_syllabus, parent, false);
            return new SyllabusViewHolder(view, this); // Pass the adapter
        } else if (viewType == LINK_TYPE) {
            View view = inflater.inflate(R.layout.item_link, parent, false);
            return new LinkViewHolder(view); // Use LinkViewHolder for LINK_TYPE
        } else {
            throw new IllegalArgumentException("Invalid view type");
        }
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ListItem item = itemList.get(position);

        if (holder instanceof PdfInfoViewHolder) {
            ((PdfInfoViewHolder) holder).bind((PdfInfo) item);
        } else if (holder instanceof AnnouncementViewHolder) {
            ((AnnouncementViewHolder) holder).bind((Announcement) item);
        } else if (holder instanceof SyllabusViewHolder) { // Handle syllabus
            ((SyllabusViewHolder) holder).bind((SyllabusInfo) item);
        } else if (holder instanceof LinkViewHolder) {
            ((LinkViewHolder) holder).bind((LinkInfo) item);
        } else {
            throw new IllegalArgumentException("Invalid view type");
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    // ViewHolder for PdfInfo
    public class PdfInfoViewHolder extends RecyclerView.ViewHolder {
        TextView pdfTitle;
        TextView dateTime;
        ImageView menuButton;
        TextView viewCount;

        public PdfInfoViewHolder(View itemView) {
            super(itemView);
            pdfTitle = itemView.findViewById(R.id.moduleTitle);
            dateTime = itemView.findViewById(R.id.dateTime);
            menuButton = itemView.findViewById(R.id.menuButton);
            viewCount = itemView.findViewById(R.id.viewCount);

            // Handle item click to view PDF
            itemView.setOnClickListener(v -> {
                PdfInfo pdfInfo = (PdfInfo) itemList.get(getAdapterPosition());
                openPdfViewer(pdfInfo);
            });

            // Handle menu button click to display both options
            menuButton.setOnClickListener(v -> {
                PdfInfo pdfInfo = (PdfInfo) itemList.get(getAdapterPosition());
                showCombinedPopupMenu(menuButton, pdfInfo, getAdapterPosition());
            });

        }

        public void bind(PdfInfo pdfInfo) {
            pdfTitle.setText(pdfInfo.getTitle());
            dateTime.setText(pdfInfo.getDateTime());

            // Set the view count, converting the integer to a string
            viewCount.setText(String.valueOf(pdfInfo.getViewCount()));
        }
    }

    public class AnnouncementViewHolder extends RecyclerView.ViewHolder {
        TextView announcementText, expandTextView, dateTimeText;
        ImageView menuButton;

        public AnnouncementViewHolder(View itemView) {
            super(itemView);

            announcementText = itemView.findViewById(R.id.announcementText);
            expandTextView = itemView.findViewById(R.id.expandTextView);
            dateTimeText = itemView.findViewById(R.id.dateTime);
            menuButton = itemView.findViewById(R.id.menuButton);

            // Set click listener for expand/collapse
            expandTextView.setOnClickListener(v -> {
                Announcement announcement = (Announcement) itemList.get(getAdapterPosition());

                if (announcement.isExpanded()) {
                    announcementText.setMaxLines(3);  // Collapse to 3 lines
                    setExpandCollapseText("Show Details", R.drawable.baseline_expand_more_24);
                    announcement.setExpanded(false);
                } else {
                    announcementText.setMaxLines(Integer.MAX_VALUE);  // Expand to full text
                    setExpandCollapseText("Hide Details", R.drawable.baseline_expand_less_24);
                    announcement.setExpanded(true);
                }
            });

            // Set click listener for menuButton
            menuButton.setOnClickListener(v -> {
                Announcement announcement = (Announcement) itemList.get(getAdapterPosition());
                int position = getAdapterPosition();
                announcementDeletePopupMenu(menuButton, announcement, position);
            });
        }

        public void bind(Announcement announcement) {
            announcementText.setText(announcement.getAnnouncement());
            dateTimeText.setText(announcement.getDateTime());

            // Set initial expanded/collapsed state
            if (announcement.isExpanded()) {
                announcementText.setMaxLines(Integer.MAX_VALUE);  // Expanded to full text
                setExpandCollapseText("Hide Details", R.drawable.baseline_expand_less_24);
            } else {
                announcementText.setMaxLines(3);  // Collapsed to 3 lines
                setExpandCollapseText("Show Details", R.drawable.baseline_expand_more_24);
            }
        }

        private void setExpandCollapseText(String text, int drawableRes) {
            expandTextView.setText(text);
            expandTextView.setCompoundDrawablesWithIntrinsicBounds(drawableRes, 0, 0, 0);  // Set drawable on the left
        }
    }

    public class LinkViewHolder extends RecyclerView.ViewHolder {
        TextView linkText, linkTitle, linkUrl, instructionText, expandTextView, dateTimeText;
        ImageView menuButton;

        public LinkViewHolder(View itemView) {
            super(itemView);

            linkText = itemView.findViewById(R.id.linkText);
            linkTitle = itemView.findViewById(R.id.linkTitle);
            linkUrl = itemView.findViewById(R.id.urlText);
            instructionText = itemView.findViewById(R.id.instructionText);
            expandTextView = itemView.findViewById(R.id.expandTextView);
            dateTimeText = itemView.findViewById(R.id.dateTime);
            menuButton = itemView.findViewById(R.id.menuButton);

            // Set click listener for expand/collapse
            expandTextView.setOnClickListener(v -> {
                LinkInfo linkInfo = (LinkInfo) itemList.get(getAdapterPosition());

                if (linkInfo.isExpanded()) {
                    instructionText.setMaxLines(3);  // Collapse to 3 lines
                    setExpandCollapseText("Show Details", R.drawable.baseline_expand_more_24);
                    linkInfo.setExpanded(false);
                    // Show linkText and urlText when collapsed
                    linkText.setVisibility(View.GONE);
                    linkUrl.setVisibility(View.GONE);
                } else {
                    instructionText.setMaxLines(Integer.MAX_VALUE);  // Expand to full text
                    setExpandCollapseText("Hide Details", R.drawable.baseline_expand_less_24);
                    linkInfo.setExpanded(true);
                    // Hide linkText and urlText when expanded
                    linkText.setVisibility(View.VISIBLE);
                    linkUrl.setVisibility(View.VISIBLE);
                }
            });

            // Set click listener for the link URL
            linkUrl.setOnClickListener(v -> {
                String url = ((LinkInfo) itemList.get(getAdapterPosition())).getLink();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                itemView.getContext().startActivity(intent);
            });

            // Set click listener for menuButton
            menuButton.setOnClickListener(v -> {
                LinkInfo linkInfo = (LinkInfo) itemList.get(getAdapterPosition());
                int position = getAdapterPosition();
                linkDeletePopupMenu(menuButton, linkInfo, position);
            });
        }

        public void bind(LinkInfo linkInfo) {
            linkTitle.setText(linkInfo.getLinkTitle());
            linkUrl.setText(linkInfo.getLink());
            instructionText.setText(linkInfo.getInstructions());
            dateTimeText.setText(linkInfo.getDateTime());

            // Set initial expanded/collapsed state
            if (linkInfo.isExpanded()) {
                instructionText.setMaxLines(Integer.MAX_VALUE);  // Expanded to full text
                setExpandCollapseText("Hide Details", R.drawable.baseline_expand_less_24);

            } else {
                instructionText.setMaxLines(3);  // Collapsed to 3 lines
                setExpandCollapseText("Show Details", R.drawable.baseline_expand_more_24);

            }
        }

        private void setExpandCollapseText(String text, int drawableRes) {
            expandTextView.setText(text);
            expandTextView.setCompoundDrawablesWithIntrinsicBounds(drawableRes, 0, 0, 0);  // Set drawable on the left
        }
    }

    public class SyllabusViewHolder extends RecyclerView.ViewHolder {
        TextView syllabusTitle;
        TextView dateTime;
        ImageView menuButton;
        TextView viewCount;
        private HomeAdapter adapter; // Add this

        public SyllabusViewHolder(View itemView, HomeAdapter adapter) {
            super(itemView);
            this.adapter = adapter; // Initialize adapter reference
            syllabusTitle = itemView.findViewById(R.id.syllabusTitle);
            dateTime = itemView.findViewById(R.id.dateTime);
            menuButton = itemView.findViewById(R.id.menuButton);
            viewCount = itemView.findViewById(R.id.viewCount);

            // Handle item click to view PDF
            itemView.setOnClickListener(v -> {
                SyllabusInfo syllabusInfo = (SyllabusInfo) adapter.getItemList().get(getAdapterPosition());
                openSyllabusViewer(syllabusInfo);
            });

            // Handle menu button click to delete Course Syllabus
            menuButton.setOnClickListener(v -> {
                SyllabusInfo syllabusInfo = (SyllabusInfo) itemList.get(getAdapterPosition());
                syllabusDeletePopupMenu(menuButton, syllabusInfo, getAdapterPosition());
            });
        }

        public void bind(SyllabusInfo syllabusInfo) {
            syllabusTitle.setText(syllabusInfo.getTitle());
            dateTime.setText(syllabusInfo.getDateTime());
            // Set the view count, converting the integer to a string
            viewCount.setText(String.valueOf(syllabusInfo.getViewCount()));
        }
    }

    private void openPdfViewer(PdfInfo pdfInfo) {
        Intent intent = new Intent(context, PdfViewerActivity.class);
        intent.putExtra("fileName", pdfInfo.getTitle());
        intent.putExtra("downloadUrl", pdfInfo.getUrl());
        context.startActivity(intent);
    }

    private void openSyllabusViewer(SyllabusInfo syllabusInfo) {
        Intent intent = new Intent(context, PdfViewerActivity.class);
        intent.putExtra("fileName", syllabusInfo.getTitle());
        intent.putExtra("downloadUrl", syllabusInfo.getUrl());
        context.startActivity(intent);
    }

    private void moduleDeletePopupMenu(ImageView menuButton, PdfInfo pdfInfo, int position) {
        ModuleDeleteHandler deleteHandler = new ModuleDeleteHandler(context, roomId, this);
        deleteHandler.showDeletePopupMenu(menuButton, pdfInfo, position);
    }

    private void addReferencesPopupMenu(ImageView menuButton, PdfInfo pdfInfo, int position) {
        String moduleId = pdfInfo.getModuleId();  // Assuming PdfInfo has a getModuleId method
        AddReferencesHandler addReferences = new AddReferencesHandler(context, roomId, moduleId);
        addReferences.handleAddReferencesClick(menuButton, pdfInfo, position);  // Call with parameters
    }

    private void editReferencesPopupMenu(ImageView menuButton, PdfInfo pdfInfo, int position) {
        String moduleId = pdfInfo.getModuleId();  // Assuming PdfInfo has a getModuleId method
        EditReferencesHandler editReferencesHandler = new EditReferencesHandler(context, roomId, moduleId);
        editReferencesHandler.handleEditReferencesClick(menuButton, pdfInfo, position);  // Call with parameters
    }

    private void showReferencesPopupMenu(ImageView menuButton, PdfInfo pdfInfo, int position) {
        String moduleId = pdfInfo.getModuleId();  // Assuming PdfInfo has a getModuleId method
        DisplayReferencesHandler displayReferencesHandler = new DisplayReferencesHandler(context, roomId, moduleId);
        displayReferencesHandler.handleShowReferencesClick(menuButton, pdfInfo, position);  // Call with parameters
    }

    private void showCombinedPopupMenu(ImageView menuButton, PdfInfo pdfInfo, int position) {
        PopupMenu popup = new PopupMenu(context, menuButton);

        // Inflate a combined menu that includes both delete and add references options
        popup.getMenuInflater().inflate(R.menu.module_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_delete_module) {
                // Call delete PDF logic
                moduleDeletePopupMenu(menuButton, pdfInfo, position);
                return true;
            } else if (item.getItemId() == R.id.action_add_references) {
                // Call add references logic
                addReferencesPopupMenu(menuButton, pdfInfo, position);
                return true;
            } else if (item.getItemId() == R.id.action_edit_references) {
                // Call show references logic
                editReferencesPopupMenu(menuButton, pdfInfo, position);
                return true;
            } else if (item.getItemId() == R.id.action_show_references) {
                // Call show references logic
                showReferencesPopupMenu(menuButton, pdfInfo, position);
                return true;
            } else {
                return false;
            }
        });
        popup.show();
    }

    private void syllabusDeletePopupMenu(ImageView menuButton, SyllabusInfo syllabusInfo, int position) {
        SyllabusDeleteHandler deleteHandler = new SyllabusDeleteHandler(context, roomId, this);
        deleteHandler.showDeletePopupMenu(menuButton, syllabusInfo, position);
    }

    private void linkDeletePopupMenu(ImageView menuButton, LinkInfo linkInfo, int position) {
        LinkDeleteHandler deleteHandler = new LinkDeleteHandler(context, roomId, this);
        deleteHandler.showDeletePopupMenu(menuButton, linkInfo, position);
    }

    private void announcementDeletePopupMenu(ImageView menuButton, Announcement announcement, int position) {
        AnnouncementDeleteHandler deleteHandler = new AnnouncementDeleteHandler(context, roomId, this);
        deleteHandler.showDeletePopupMenu(menuButton, announcement, position);
    }

    // Method to add a new item and refresh the adapter
    public void addItem(ListItem newItem) {
        itemList.add(newItem);
        sortItemsByDate(); // Re-sort after adding
        notifyDataSetChanged(); // Notify adapter about data change
    }
}
