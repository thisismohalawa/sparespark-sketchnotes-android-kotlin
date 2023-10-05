package sparespark.sketchnotes.notes.notedetail

import sparespark.sketchnotes.data.model.note.Note

sealed class NoteDetailEvent {
    object OnDestroy : NoteDetailEvent()
    object UpdateBottomSheetToHideState : NoteDetailEvent()
    object UpdateBottomSheetToExpandState : NoteDetailEvent()
    data class OnNoteViewStartGetArgs(val argsNote: Note) : NoteDetailEvent()
    data class UpdateNoteCardColor(val hexColor: String) : NoteDetailEvent()
    data class OnUpdateNoteClicked(val content: String, val title: String, val email: String) :
        NoteDetailEvent()

    object DeleteNote : NoteDetailEvent()
    data class OnAddEmailIconClicked(val email: String) : NoteDetailEvent()

}
