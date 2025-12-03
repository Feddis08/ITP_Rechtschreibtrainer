package at.tgm.client.quiz;

import at.tgm.objects.FachbegriffItem;

public interface QuizFinishedListener {
    void onQuizFinished(FachbegriffItem[] answeredItems);
}
