package sparespark.sketchnotes.notes.notefilter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlinx.android.synthetic.main.notefilter_view.mt_searchView
import kotlinx.android.synthetic.main.notefilter_view.rec_fil_note_list
import sparespark.sketchnotes.R
import sparespark.sketchnotes.core.view.Communicator
import sparespark.sketchnotes.core.view.makeToast
import sparespark.sketchnotes.core.view.relaunchCurrentView
import sparespark.sketchnotes.core.view.setNoteRecyclerListBehavioral
import sparespark.sketchnotes.data.model.note.Note
import sparespark.sketchnotes.notes.notelist.NoteListEvent
import sparespark.sketchnotes.notes.notelist.adapter.NoteListAdapter
import sparespark.sketchnotes.notes.notelist.buildlogic.NoteListInjector
import sparespark.sketchnotes.notes.notelist.viewmodel.NoteListViewModel
import java.util.Locale

class NoteFilterView : Fragment() {

    private lateinit var commView: Communicator.View
    private lateinit var listAdapter: NoteListAdapter
    private lateinit var noteViewModel: NoteListViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.notefilter_view, container, false)


    private fun navigateToNoteDetailsWithArgs(note: Note) =
        if (findNavController().currentDestination?.id == R.id.noteFilterView) {
            findNavController().navigate(
                NoteFilterViewDirections.toNoteDetailView(note = note)
            )
        } else Unit


    override fun onDestroyView() {
        super.onDestroyView()
        rec_fil_note_list.adapter = null
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
    }

    private fun initializeBaseArg() {
        noteViewModel = ViewModelProvider(
            this, NoteListInjector(requireActivity().application).provideNoteListViewModelFactory()
        )[NoteListViewModel::class.java]
        noteViewModel.handleEvent(NoteListEvent.GetFilteredNotes)
    }

    private fun initializeViewArg() {
        commView = activity as Communicator.View
    }

    private fun setupNoteRecyclerView() {
        listAdapter = NoteListAdapter(displayInfoItems = false)
        rec_fil_note_list.apply {
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
        noteItemClicked.observe(viewLifecycleOwner) {
            navigateToNoteDetailsWithArgs(note = it)
        }
        noteList.observe(viewLifecycleOwner) { noteList ->
            if (noteList?.isNotEmpty() == true) {
                listAdapter.submitList(noteList)
                setUpSearchViewListener(noteList)
            }
        }
        deleted.observe(viewLifecycleOwner) {
            if (it) relaunchCurrentView()
        }
    }

    private fun setUpSearchViewListener(noteList: List<Note>) {
        mt_searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { filterList(it, noteList) }
                return true
            }
        })
    }

    private fun filterList(query: String, noteList: List<Note>) {
        val filteredList = mutableListOf<Note>()
        for (note in noteList) if (note.content.lowercase(Locale.ROOT)
                .contains(query)
        ) filteredList.add(note)

        if (filteredList.isNotEmpty()) listAdapter.submitList(filteredList)
    }

    private fun setUpClickListeners() {
        val callback = object : OnBackPressedCallback(
            true
        ) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }
}
