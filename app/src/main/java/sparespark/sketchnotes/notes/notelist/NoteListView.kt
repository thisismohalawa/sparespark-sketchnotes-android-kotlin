package sparespark.sketchnotes.notes.notelist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.notelist_view.fab_search
import kotlinx.android.synthetic.main.notelist_view.rec_note_List
import sparespark.sketchnotes.R
import sparespark.sketchnotes.core.ABOUT_APP_POS_ID
import sparespark.sketchnotes.core.ADD_NEW_NOTE_POS_ID
import sparespark.sketchnotes.core.SIGN_POS_ID
import sparespark.sketchnotes.core.view.Communicator
import sparespark.sketchnotes.core.view.makeToast
import sparespark.sketchnotes.core.view.relaunchCurrentView
import sparespark.sketchnotes.core.view.setNoteRecyclerListBehavioral
import sparespark.sketchnotes.data.model.note.Note
import sparespark.sketchnotes.notes.NoteActivity
import sparespark.sketchnotes.notes.notelist.adapter.NoteListAdapter
import sparespark.sketchnotes.notes.notelist.buildlogic.NoteListInjector
import sparespark.sketchnotes.notes.notelist.viewmodel.NoteListViewModel

class NoteListView : Fragment(), View.OnClickListener {

    private lateinit var commView: Communicator.View
    private lateinit var commAction: Communicator.Action
    private lateinit var listAdapter: NoteListAdapter
    private lateinit var noteViewModel: NoteListViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View? = inflater.inflate(R.layout.notelist_view, container, false)

    private fun navigateToNoteDetailWithArgs(note: Note) =
        if (findNavController().currentDestination?.id == R.id.noteListView) findNavController().navigate(
            NoteListViewDirections.toNoteDetailView(note)
        ) else Unit

    private fun navigateToNoteFilterView() =
        if (findNavController().currentDestination?.id == R.id.noteListView) findNavController().navigate(
            NoteListViewDirections.toNoteFilterView()
        ) else Unit

    private fun navigateToLoginView() =
        if (findNavController().currentDestination?.id == R.id.noteListView) findNavController().navigate(
            NoteListViewDirections.toLoginActivity()
        ) else Unit

    private fun onNoteItemClicked(note: Note) {
        when (note.pos) {
            ADD_NEW_NOTE_POS_ID -> navigateToNoteDetailWithArgs(newNote(null))
            ABOUT_APP_POS_ID -> {}
            SIGN_POS_ID -> navigateToLoginView()
            else -> navigateToNoteDetailWithArgs(note = note)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.fab_search -> navigateToNoteFilterView()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        rec_note_List.adapter = null
    }

    override fun onDestroy() {
        super.onDestroy()
        noteViewModel.handleEvent(NoteListEvent.OnDestroy)
    }

    override fun onStart() {
        super.onStart()
        initializeViewArg()
        initializeBaseArg()
        setupNoteRecyclerView()
        noteViewModel.dataObserving()
        setUpClickListeners()
        checkIntentSentClipData()
    }

    private fun initializeBaseArg() {
        noteViewModel = ViewModelProvider(
            this, NoteListInjector(requireActivity().application).provideNoteListViewModelFactory()
        )[NoteListViewModel::class.java]
        noteViewModel.handleEvent(NoteListEvent.OnNoteListViewStart(requireContext()))
        noteViewModel.handleEvent(NoteListEvent.UpdateRemoteToken)
    }

    private fun initializeViewArg() {
        commAction = activity as Communicator.Action
        commView = activity as Communicator.View
    }

    private fun setupNoteRecyclerView() {
        listAdapter = NoteListAdapter(displayInfoItems = true)
        rec_note_List.apply {
            setNoteRecyclerListBehavioral()
            adapter = listAdapter
        }
        listAdapter.event.observe(
            viewLifecycleOwner
        ) {
            noteViewModel.handleEvent(it)
        }
    }

    private fun NoteListViewModel.dataObserving() {
        error.observe(viewLifecycleOwner) {
            activity?.makeToast(it.asString(context))
        }
        loading.observe(viewLifecycleOwner) {
            if (it) commView.showProgress()
            else commView.hideProgress()
        }
        noteList.observe(viewLifecycleOwner) { noteList ->
            if (noteList?.isNotEmpty() == true) listAdapter.submitList(noteList)
        }
        noteItemClicked.observe(viewLifecycleOwner) {
            onNoteItemClicked(note = it)
        }
        deleted.observe(viewLifecycleOwner) {
            if (it) relaunchCurrentView()
        }
    }

    private fun setUpClickListeners() {
        fab_search.setOnClickListener(this)/*
        * Whatever navigation view we are in, back-pressed in notelist view finish activity.
        * */
        val callback = object : OnBackPressedCallback(
            true
        ) {
            override fun handleOnBackPressed() {
                (activity as NoteActivity).finish()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private fun checkIntentSentClipData() {
        if (commAction.getSentIntentClipData() != null) {
            navigateToNoteDetailWithArgs(newNote(content = commAction.getSentIntentClipData()))
            NoteActivity.isClipDataReceived = true
        }
    }

    private fun newNote(content: String?): Note =
        Note(content ?: "", "", "", "", "", null, null)
}
