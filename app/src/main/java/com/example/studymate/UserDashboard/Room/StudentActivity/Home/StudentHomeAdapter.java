package com.example.studymate.UserDashboard.Room.StudentActivity.Home;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studymate.Dialog.DisplayReferencesHandler;
import com.example.studymate.R;
import com.example.studymate.UserDashboard.Room.FacultyActivity.Home.SyllabusInfo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Collections;
import java.util.List;

public class StudentHomeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int PDF_INFO_TYPE = 1;
    private static final int ANNOUNCEMENT_TYPE = 2;
    private static final int SYLLABUS_TYPE = 3;
    private static final int LINK_TYPE = 4;


    private List<ListItem> itemList;
    private Context context;
    private String roomId; // Assuming you need roomId for deleting the item

    public StudentHomeAdapter(Context context, List<ListItem> itemList, String roomId) {
        this.context = context;
        this.itemList = itemList;
        this.roomId = roomId;
        sortItemsByDate(); // Sort items initially
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
        } else if (item instanceof StudentSyllabusInfo) {
            return ((StudentSyllabusInfo) item).getTimestamp(); // Use timestamp for sorting
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
            return new PdfInfoViewHolder(view, this);
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
            ((SyllabusViewHolder) holder).bind((StudentSyllabusInfo) item);
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
        private StudentHomeAdapter adapter; // Reference to the adapter

        public PdfInfoViewHolder(View itemView, StudentHomeAdapter adapter) {
            super(itemView);
            this.adapter = adapter;
            pdfTitle = itemView.findViewById(R.id.moduleTitle);
            dateTime = itemView.findViewById(R.id.dateTime);
            menuButton = itemView.findViewById(R.id.menuButton);
            viewCount = itemView.findViewById(R.id.viewCount);

            // Handle item click to view PDF
            itemView.setOnClickListener(v -> {
                PdfInfo pdfInfo = (PdfInfo) adapter.getItemList().get(getAdapterPosition());
                openPdf(pdfInfo);
            });

            // Handle menu button click to display both options
            menuButton.setOnClickListener(v -> {
                PdfInfo pdfInfo = (PdfInfo) adapter.getItemList().get(getAdapterPosition());
                showReferencesPopupMenu(menuButton, pdfInfo, getAdapterPosition());
            });
        }

        public void bind(PdfInfo pdfInfo) {
            pdfTitle.setText(pdfInfo.getTitle());
            dateTime.setText(pdfInfo.getDateTime());
            // Set the view count, converting the integer to a string
            viewCount.setText(String.valueOf(pdfInfo.getViewCount()));
        }

        private void openPdf(PdfInfo pdfInfo) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Get current user ID
            DatabaseReference pdfRef = FirebaseDatabase.getInstance().getReference("Home")
                    .child(roomId)
                    .child(pdfInfo.getModuleId());

            // Check if viewerList already has this userId
            pdfRef.child("viewerList").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // User has already viewed the PDF
                        openPdfViewer(pdfInfo);
                        Toast.makeText(itemView.getContext(), "You have already opened this PDF.", Toast.LENGTH_SHORT).show();
                    } else {
                        // User has not viewed the PDF yet
                        pdfRef.child("viewCount").get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Long currentViewCount = task.getResult().getValue(Long.class);
                                if (currentViewCount == null) currentViewCount = 0L;

                                // Increment the view count
                                pdfRef.child("viewCount").setValue(currentViewCount + 1)
                                        .addOnSuccessListener(aVoid -> {
                                            // After updating viewCount, add userId to viewerList
                                            pdfRef.child("viewerList").child(userId).setValue(true);
                                            Toast.makeText(itemView.getContext(), "PDF opened for the first time!", Toast.LENGTH_SHORT).show();

                                            openPdfViewer(pdfInfo);
                                        })
                                        .addOnFailureListener(e -> {
                                            // Handle failure to update viewCount
                                            Toast.makeText(itemView.getContext(), "Failed to update view count.", Toast.LENGTH_SHORT).show();
                                        });
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(itemView.getContext(), "Error accessing database.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public class AnnouncementViewHolder extends RecyclerView.ViewHolder {
        TextView announcementText, expandTextView, dateTimeText;

        public AnnouncementViewHolder(View itemView) {
            super(itemView);

            announcementText = itemView.findViewById(R.id.announcementText);
            expandTextView = itemView.findViewById(R.id.expandTextView);
            dateTimeText = itemView.findViewById(R.id.dateTime);

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

            menuButton.setVisibility(View.GONE);

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

        }

        public void bind(LinkInfo linkInfo) {
            linkTitle.setText(linkInfo.getLinkTitle());
            linkUrl.setText(linkInfo.getLink());
            instructionText.setText(linkInfo.getInstructions());
            dateTimeText.setText(linkInfo.getDateTime());

            menuButton.setVisibility(View.GONE);

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
        private StudentHomeAdapter adapter;

        public SyllabusViewHolder(View itemView, StudentHomeAdapter adapter) {
            super(itemView);
            this.adapter = adapter;
            syllabusTitle = itemView.findViewById(R.id.syllabusTitle);
            dateTime = itemView.findViewById(R.id.dateTime);
            menuButton = itemView.findViewById(R.id.menuButton);
            viewCount = itemView.findViewById(R.id.viewCount);

            // Handle item click to view PDF
            itemView.setOnClickListener(v -> {
                StudentSyllabusInfo studentSyllabusInfo = (StudentSyllabusInfo) adapter.getItemList().get(getAdapterPosition());
                openSyllabus(studentSyllabusInfo);
            });
        }

        // Bind method to set the data to the views
        public void bind(StudentSyllabusInfo syllabusInfo) {
            syllabusTitle.setText(syllabusInfo.getTitle());
            dateTime.setText(syllabusInfo.getDateTime());

            // Set the view count, converting the integer to a string
            viewCount.setText(String.valueOf(syllabusInfo.getViewCount()));
        }

        private void openSyllabus(StudentSyllabusInfo studentSyllabusInfo) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Get current user ID
            DatabaseReference syllabusRef = FirebaseDatabase.getInstance().getReference("Home")
                    .child(roomId)
                    .child(studentSyllabusInfo.getId());

            // Check if viewerList already has this userId
            syllabusRef.child("viewerList").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // User has already viewed the syllabus
                        openSyllabusViewer(studentSyllabusInfo);
                        Toast.makeText(itemView.getContext(), "You have already opened this syllabus.", Toast.LENGTH_SHORT).show();
                    } else {
                        // User has not viewed the syllabus yet
                        syllabusRef.child("viewCount").get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Long currentViewCount = task.getResult().getValue(Long.class);
                                if (currentViewCount == null) currentViewCount = 0L;

                                // Increment the view count
                                syllabusRef.child("viewCount").setValue(currentViewCount + 1)
                                        .addOnSuccessListener(aVoid -> {
                                            // After updating viewCount, add userId to viewerList
                                            syllabusRef.child("viewerList").child(userId).setValue(true);
                                            Toast.makeText(itemView.getContext(), "Syllabus opened for the first time!", Toast.LENGTH_SHORT).show();

                                            openSyllabusViewer(studentSyllabusInfo);
                                        })
                                        .addOnFailureListener(e -> {
                                            // Handle failure to update viewCount
                                            Toast.makeText(itemView.getContext(), "Failed to update view count.", Toast.LENGTH_SHORT).show();
                                        });
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(itemView.getContext(), "Error accessing database.", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    private void showReferencesPopupMenu(ImageView menuButton, PdfInfo pdfInfo, int position) {
        // Create a popup menu
        PopupMenu popup = new PopupMenu(context, menuButton);
        popup.inflate(R.menu.references_menu); // Inflate the menu resource

        // Handle menu item clicks
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_show_references) {
                // Call the logic to display student references
                String moduleId = pdfInfo.getModuleId();  // Ensure PdfInfo has a getModuleId method
                DisplayReferencesHandler displayReferencesHandler = new DisplayReferencesHandler(context, roomId, moduleId);
                displayReferencesHandler.handleShowStudentReferencesClick(menuButton, pdfInfo, position);  // Call the method
                return true; // Return true to indicate item click handled
            }
            return false;
        });

        // Show the popup menu
        popup.show();
    }


    private void openPdfViewer(PdfInfo pdfInfo) {
        Intent intent = new Intent(context, ModuleViewerActivity.class);
        intent.putExtra("fileName", pdfInfo.getTitle());
        intent.putExtra("downloadUrl", pdfInfo.getUrl());
        context.startActivity(intent);
    }

    private void openSyllabusViewer(StudentSyllabusInfo syllabusInfo) {
        Intent intent = new Intent(context, ModuleViewerActivity.class);
        intent.putExtra("fileName", syllabusInfo.getTitle());
        intent.putExtra("downloadUrl", syllabusInfo.getUrl());
        context.startActivity(intent);
    }
}
