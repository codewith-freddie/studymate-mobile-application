package com.example.studymate.UserDashboard.Room.FacultyActivity.Quiz;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studymate.R;

import java.util.List;

import android.widget.Button;

public class QuestionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_QUESTION = 0;
    private static final int TYPE_FOOTER = 1;

    private List<Question> questionList;

    public QuestionAdapter(List<Question> questionList) {
        this.questionList = questionList;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == questionList.size()) {
            return TYPE_FOOTER;
        } else {
            return TYPE_QUESTION;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_FOOTER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_back, parent, false);
            return new FooterViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.display_question_card, parent, false);
            return new QuestionViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == TYPE_QUESTION) {
            Question question = questionList.get(position);
            QuestionViewHolder questionHolder = (QuestionViewHolder) holder;
            questionHolder.question.setText(question.getQuestion());
            questionHolder.option1.setText(question.getOption1());
            questionHolder.option2.setText(question.getOption2());
            questionHolder.option3.setText(question.getOption3());
            questionHolder.option4.setText(question.getOption4());
        } else if (holder.getItemViewType() == TYPE_FOOTER) {
            FooterViewHolder footerHolder = (FooterViewHolder) holder;
            footerHolder.backButton.setOnClickListener(v -> {
                // Handle back button click
                ((QuizDetailActivity) v.getContext()).onBackPressed();
            });
        }
    }

    @Override
    public int getItemCount() {
        return questionList.size() + 1; // Add one for the footer
    }

    public static class QuestionViewHolder extends RecyclerView.ViewHolder {
        TextView question;
        RadioGroup radioGroup;
        TextView option1, option2, option3, option4;

        public QuestionViewHolder(@NonNull View itemView) {
            super(itemView);
            question = itemView.findViewById(R.id.displayQuestion);
            option1 = itemView.findViewById(R.id.displayOption1);
            option2 = itemView.findViewById(R.id.displayOption2);
            option3 = itemView.findViewById(R.id.displayOption3);
            option4 = itemView.findViewById(R.id.displayOption4);
            radioGroup = itemView.findViewById(R.id.radioGroup);
        }
    }

    public static class FooterViewHolder extends RecyclerView.ViewHolder {
        Button backButton;

        public FooterViewHolder(@NonNull View itemView) {
            super(itemView);
            backButton = itemView.findViewById(R.id.back_button);
        }
    }
}

