package com.example.studymate.UserDashboard.Room.FacultyActivity.QuizDisplay;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studymate.R;
import java.util.List;

public class QuizQuestionDisplayAdapter extends RecyclerView.Adapter<QuizQuestionDisplayAdapter.ViewHolder> {

    private List<QuizQuestionDisplay> quizQuestions;
    private String roomId;
    private String quizId;

    public QuizQuestionDisplayAdapter(List<QuizQuestionDisplay> quizQuestions, String roomId, String quizId) {
        this.quizQuestions = quizQuestions;
        this.roomId = roomId;
        this.quizId = quizId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.quiz_question_display_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        QuizQuestionDisplay quizQuestion = quizQuestions.get(position);
        holder.textViewQuestion.setText(quizQuestion.getQuestionText());

        // Set each button's text to the options
        List<String> options = quizQuestion.getOptionList();
        String option1Text = options.get(0);
        String option2Text = options.get(1);
        String option3Text = options.get(2);
        String option4Text = options.get(3);

        holder.buttonOption1.setText(option1Text);
        holder.buttonOption2.setText(option2Text);
        holder.buttonOption3.setText(option3Text);
        holder.buttonOption4.setText(option4Text);

        // Reset button colors to default
        int defaultColor = holder.itemView.getContext().getResources().getColor(R.color.cream_white);
        int defaultTextColor = holder.itemView.getContext().getResources().getColor(R.color.light_gray);
        holder.buttonOption1.setBackgroundColor(defaultColor);
        holder.buttonOption2.setBackgroundColor(defaultColor);
        holder.buttonOption3.setBackgroundColor(defaultColor);
        holder.buttonOption4.setBackgroundColor(defaultColor);
        holder.buttonOption1.setTextColor(defaultTextColor);
        holder.buttonOption2.setTextColor(defaultTextColor);
        holder.buttonOption3.setTextColor(defaultTextColor);
        holder.buttonOption4.setTextColor(defaultTextColor);

        // Retrieve the correct answer text
        String correctAnswer = quizQuestion.getCorrectAnswer();
        int correctColor = holder.itemView.getContext().getResources().getColor(R.color.green);
        int correctTextColor = holder.itemView.getContext().getResources().getColor(R.color.cream_white);

        // Compare correctAnswer with each button's text and highlight the correct one
        if (option1Text.equals(correctAnswer)) {
            holder.buttonOption1.setBackgroundColor(correctColor);
            holder.buttonOption1.setTextColor(correctTextColor);
        } else if (option2Text.equals(correctAnswer)) {
            holder.buttonOption2.setBackgroundColor(correctColor);
            holder.buttonOption2.setTextColor(correctTextColor);
        } else if (option3Text.equals(correctAnswer)) {
            holder.buttonOption3.setBackgroundColor(correctColor);
            holder.buttonOption3.setTextColor(correctTextColor);
        } else if (option4Text.equals(correctAnswer)) {
            holder.buttonOption4.setBackgroundColor(correctColor); // Corrected this line
            holder.buttonOption4.setTextColor(correctTextColor);    // Corrected this line
        }
    }


    @Override
    public int getItemCount() {
        return quizQuestions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewQuestion;
        Button buttonOption1, buttonOption2, buttonOption3, buttonOption4;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewQuestion = itemView.findViewById(R.id.textViewQuestion);
            buttonOption1 = itemView.findViewById(R.id.buttonOption1);
            buttonOption2 = itemView.findViewById(R.id.buttonOption2);
            buttonOption3 = itemView.findViewById(R.id.buttonOption3);
            buttonOption4 = itemView.findViewById(R.id.buttonOption4);
        }
    }
}
