package com.example.studymate.UserDashboard.Room.FacultyActivity.Quiz;

import android.content.res.ColorStateList;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studymate.R;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import android.widget.RadioButton;

public class QuizAdapter extends RecyclerView.Adapter<QuizAdapter.QuizViewHolder> {
    private final List<QuizQuestion> quizQuestions;
    private final int defaultColor;
    private final RecyclerView recyclerView;
    RadioButton radio_button1, radio_button2, radio_button3, radio_button4;

    public QuizAdapter(List<QuizQuestion> quizQuestions, int defaultColor, RecyclerView recyclerView) {
        this.quizQuestions = quizQuestions;
        this.defaultColor = defaultColor;
        this.recyclerView = recyclerView;
    }

    @NonNull
    @Override
    public QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.question_edit, parent, false);
        return new QuizViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuizViewHolder holder, int position) {
        QuizQuestion question = quizQuestions.get(position);
        holder.bind(question); // Call the bind method to populate the fields
    }

    @Override
    public int getItemCount() {
        return quizQuestions.size();
    }

    public List<Integer> validateFields() {
        List<Integer> invalidPositions = new ArrayList<>();
        for (int i = 0; i < quizQuestions.size(); i++) {
            QuizQuestion question = quizQuestions.get(i);
            if (question.isInvalid()) {
                invalidPositions.add(i);
            }
        }
        return invalidPositions;
    }

    class QuizViewHolder extends RecyclerView.ViewHolder {
        private final EditText questionEditText, option1EditText, option2EditText, option3EditText, option4EditText;
        private final RadioGroup optionsRadioGroup;
        private final View cardLayout;
        private final ImageButton deleteButton;

        QuizViewHolder(@NonNull View itemView) {
            super(itemView);
            questionEditText = itemView.findViewById(R.id.question);
            option1EditText = itemView.findViewById(R.id.option1et);
            option2EditText = itemView.findViewById(R.id.option2et);
            option3EditText = itemView.findViewById(R.id.option3et);
            option4EditText = itemView.findViewById(R.id.option4et);

            cardLayout = itemView.findViewById(R.id.cardLayout);
            deleteButton = itemView.findViewById(R.id.deleteButton);

            optionsRadioGroup = itemView.findViewById(R.id.radioGroup);
            radio_button1 = itemView.findViewById(R.id.radio_button1);
            radio_button2 = itemView.findViewById(R.id.radio_button2);
            radio_button3 = itemView.findViewById(R.id.radio_button3);
            radio_button4 = itemView.findViewById(R.id.radio_button4);

            deleteButton.setOnClickListener(v -> {
                // Remove the question from the list and notify the adapter
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    quizQuestions.remove(position); // Remove the question
                    notifyItemRemoved(position); // Notify the adapter about the removed item
                    notifyItemRangeChanged(position, quizQuestions.size()); // Update the remaining items
                }
            });
        }

        void bind(QuizQuestion question) {
            questionEditText.setText(question.getQuestion());

            // Set options from the list
            List<String> options = question.getOptions();
            if (options.size() > 0) {
                option1EditText.setText(options.get(0)); // First option
            } else {
                option1EditText.setText(""); // Clear if no options
            }

            if (options.size() > 1) {
                option2EditText.setText(options.get(1)); // Second option
            } else {
                option2EditText.setText(""); // Clear if no options
            }

            if (options.size() > 2) {
                option3EditText.setText(options.get(2)); // Third option
            } else {
                option3EditText.setText(""); // Clear if no options
            }

            if (options.size() > 3) {
                option4EditText.setText(options.get(3)); // Fourth option
            } else {
                option4EditText.setText(""); // Clear if no options
            }

            // Text Watchers for options and question
            questionEditText.addTextChangedListener(createTextWatcher(question::setQuestion));
            option1EditText.addTextChangedListener(createTextWatcher(s -> updateOption(question, s, 0)));
            option2EditText.addTextChangedListener(createTextWatcher(s -> updateOption(question, s, 1)));
            option3EditText.addTextChangedListener(createTextWatcher(s -> updateOption(question, s, 2)));
            option4EditText.addTextChangedListener(createTextWatcher(s -> updateOption(question, s, 3)));

            optionsRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                String correctAnswer = "";
                switch (getOptionNumber(checkedId)) {
                    case 1:
                        correctAnswer = option1EditText.getText().toString();
                        break;
                    case 2:
                        correctAnswer = option2EditText.getText().toString();
                        break;
                    case 3:
                        correctAnswer = option3EditText.getText().toString();
                        break;
                    case 4:
                        correctAnswer = option4EditText.getText().toString();
                        break;
                }
                question.setCorrect(correctAnswer);
                updateCardBackground(question);
            });

            updateCardBackground(question);
        }

        private void updateOption(QuizQuestion question, String optionText, int index) {
            List<String> options = question.getOptions();
            if (index < options.size()) {
                options.set(index, optionText); // Update the corresponding option
            } else {
                // If index exceeds current size, add a new option if needed
                while (options.size() <= index) {
                    options.add(""); // Fill in empty strings until reaching desired index
                }
                options.set(index, optionText);
            }
            question.setOptions(options); // Update the question's options
            updateCardBackground(question);
        }

        private TextWatcher createTextWatcher(Consumer<String> setter) {
            return new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    setter.accept(s.toString());
                    updateCardBackground(quizQuestions.get(getAdapterPosition()));
                }
            };
        }

        private int getOptionNumber(int checkedId) {
            if (checkedId == R.id.radio_button1) {
                return 1;
            } else if (checkedId == R.id.radio_button2) {
                return 2;
            } else if (checkedId == R.id.radio_button3) {
                return 3;
            } else if (checkedId == R.id.radio_button4) {
                return 4;
            }
            return -1;
        }

        private void updateCardBackground(QuizQuestion question) {
            // Check the entire card layout
            if (question.isInvalid()) {
                cardLayout.setBackgroundResource(R.drawable.error_border);

                // Highlight specific fields if they are empty
                if (question.getQuestion().trim().isEmpty()) {
                    questionEditText.setHintTextColor(itemView.getContext().getResources().getColor(R.color.red));
                } else {
                    questionEditText.setHintTextColor(itemView.getContext().getResources().getColor(R.color.green));
                }

                if (option1EditText.getText().toString().trim().isEmpty()) {
                    option1EditText.setHintTextColor(itemView.getContext().getResources().getColor(R.color.red));
                } else {
                    option1EditText.setHintTextColor(itemView.getContext().getResources().getColor(R.color.light_gray));
                }

                if (option2EditText.getText().toString().trim().isEmpty()) {
                    option2EditText.setHintTextColor(itemView.getContext().getResources().getColor(R.color.red));
                } else {
                    option2EditText.setHintTextColor(itemView.getContext().getResources().getColor(R.color.light_gray));
                }

                if (option3EditText.getText().toString().trim().isEmpty()) {
                    option3EditText.setHintTextColor(itemView.getContext().getResources().getColor(R.color.red));
                } else {
                    option3EditText.setHintTextColor(itemView.getContext().getResources().getColor(R.color.light_gray));
                }

                if (option4EditText.getText().toString().trim().isEmpty()) {
                    option4EditText.setHintTextColor(itemView.getContext().getResources().getColor(R.color.red));
                } else {
                    option4EditText.setHintTextColor(itemView.getContext().getResources().getColor(R.color.light_gray));
                }

                deleteButton.setImageTintList(ColorStateList.valueOf(itemView.getContext().getResources().getColor(R.color.red)));

                // Check if any radio button is null
                if (radio_button1 == null || radio_button2 == null || radio_button3 == null || radio_button4 == null) {
                    // Set red tint for each RadioButton in the group if any are null
                    for (int i = 0; i < optionsRadioGroup.getChildCount(); i++) {
                        View radioButton = optionsRadioGroup.getChildAt(i);
                        if (radioButton instanceof RadioButton) {
                            ((RadioButton) radioButton).setButtonTintList(
                                    ColorStateList.valueOf(itemView.getContext().getResources().getColor(R.color.red))
                            );
                        }
                    }
                } else {
                    // Set green tint for each RadioButton in the group if none are null
                    for (int i = 0; i < optionsRadioGroup.getChildCount(); i++) {
                        View radioButton = optionsRadioGroup.getChildAt(i);
                        if (radioButton instanceof RadioButton) {
                            ((RadioButton) radioButton).setButtonTintList(
                                    ColorStateList.valueOf(itemView.getContext().getResources().getColor(R.color.green))
                            );
                        }
                    }
                }


            } else {
                // Set default background if no issues
                cardLayout.setBackgroundResource(R.drawable.border);
                questionEditText.setHintTextColor(itemView.getContext().getResources().getColor(R.color.light_gray));
                option1EditText.setHintTextColor(itemView.getContext().getResources().getColor(R.color.light_gray));
                option2EditText.setHintTextColor(itemView.getContext().getResources().getColor(R.color.light_gray));
                option3EditText.setHintTextColor(itemView.getContext().getResources().getColor(R.color.light_gray));
                option4EditText.setHintTextColor(itemView.getContext().getResources().getColor(R.color.light_gray));
                deleteButton.setImageTintList(ColorStateList.valueOf(itemView.getContext().getResources().getColor(R.color.green)));

                for (int i = 0; i < optionsRadioGroup.getChildCount(); i++) {
                    View radioButton = optionsRadioGroup.getChildAt(i);
                    if (radioButton instanceof RadioButton) {
                        ((RadioButton) radioButton).setButtonTintList(
                                ColorStateList.valueOf(itemView.getContext().getResources().getColor(R.color.green))
                        );
                    }
                }

            }
        }
    }
}
