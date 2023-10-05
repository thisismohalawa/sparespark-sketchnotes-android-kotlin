package sparespark.sketchnotes.notes.notedetail

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.PopupMenu
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.notedetail_view.bottom_sheet_update_note
import kotlinx.android.synthetic.main.notedetail_view.ed_note_content
import kotlinx.android.synthetic.main.notedetail_view.ed_note_title
import kotlinx.android.synthetic.main.notedetail_view.ed_user_email
import kotlinx.android.synthetic.main.notedetail_view.im_add_email
import kotlinx.android.synthetic.main.notedetail_view.rec_list_colors
import kotlinx.android.synthetic.main.notedetail_view.sim_img_option_menu
import kotlinx.android.synthetic.main.notedetail_view.sim_lbl_content
import kotlinx.android.synthetic.main.notedetail_view.sim_lbl_owner
import kotlinx.android.synthetic.main.notedetail_view.sim_lbl_title
import kotlinx.android.synthetic.main.notedetail_view.simulate_content_card_view
import kotlinx.android.synthetic.main.notedetail_view.simulate_title_card_view
import kotlinx.android.synthetic.main.notedetail_view.tx_update_note
import sparespark.sketchnotes.R
import sparespark.sketchnotes.core.NOTE_DEF_INPUT
import sparespark.sketchnotes.core.actionCopyText
import sparespark.sketchnotes.core.actionDisplayConfirmationDialog
import sparespark.sketchnotes.core.actionOpenUrl
import sparespark.sketchnotes.core.actionShareText
import sparespark.sketchnotes.core.view.Communicator
import sparespark.sketchnotes.core.view.beginActionEnabledTextViewWatcher
import sparespark.sketchnotes.core.view.beginDoubleValueTextViewWatcher
import sparespark.sketchnotes.core.view.makeToast
import sparespark.sketchnotes.core.view.setCardBackgroundColor
import sparespark.sketchnotes.core.view.setClickListenerWithViewDelayEnabled
import sparespark.sketchnotes.core.view.setTitleColor
import sparespark.sketchnotes.core.view.toEditable
import sparespark.sketchnotes.data.model.note.Note
import sparespark.sketchnotes.notes.NoteActivity
import sparespark.sketchnotes.notes.notedetail.adapter.ColorListAdapter
import sparespark.sketchnotes.notes.notedetail.buildlogic.NoteDetailInjector
import sparespark.sketchnotes.notes.notedetail.viewmodel.NoteViewModel

class NoteDetailView : Fragment(), View.OnClickListener {
    private lateinit var commView: Communicator.View
    private lateinit var noteViewModel: NoteViewModel
    private lateinit var noteBottomSheetBehavior: BottomSheetBehavior<View>
    private val args: NoteDetailViewArgs by navArgs()
    private val currentNote: Note
        get() = args.note

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View? = inflater.inflate(R.layout.notedetail_view, container, false)

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tx_update_note -> updateNote(
                content = ed_note_content.text?.trim().toString(),
                title = ed_note_title.text?.trim().toString(),
                email = ed_user_email?.text?.trim().toString()
            )

            R.id.im_add_email -> noteViewModel.handleEvent(
                NoteDetailEvent.OnAddEmailIconClicked(
                    ed_user_email.text?.trim().toString()
                )
            )
        }
    }

    private fun navigateToNoteListView() =
        if (findNavController().currentDestination?.id == R.id.noteDetailView) findNavController().navigate(
            NoteDetailViewDirections.toNoteListView()
        ) else Unit

    private fun navigateToLoginView() = findNavController().navigate(R.id.loginActivity)

    private fun updateSheetToExpandingState() =
        noteViewModel.handleEvent(NoteDetailEvent.UpdateBottomSheetToExpandState)

    private fun updateSheetToHideState() =
        noteViewModel.handleEvent(NoteDetailEvent.UpdateBottomSheetToHideState)


    override fun onDestroy() {
        super.onDestroy()
        noteViewModel.handleEvent(NoteDetailEvent.OnDestroy)
    }

    override fun onStart() {
        super.onStart()
        initializeBottomSheetBehavior()
        initializeViewArg()
        initializeBaseArg()
        setUpColorRecyclerList()
        noteViewModel.startObserving()
        setUpViewListener()
    }

    private fun initializeBottomSheetBehavior() {
        noteBottomSheetBehavior = BottomSheetBehavior.from(bottom_sheet_update_note)
    }

    private fun initializeBaseArg() {
        noteViewModel = ViewModelProvider(
            this, NoteDetailInjector(requireActivity().application).provideNoteViewModelFactory()
        )[NoteViewModel::class.java]
        noteViewModel.handleEvent(NoteDetailEvent.OnNoteViewStartGetArgs(currentNote))
    }

    private fun initializeViewArg() {
        commView = activity as Communicator.View
    }

    private fun setUpColorRecyclerList() = rec_list_colors.apply {
        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        adapter = ColorListAdapter { hex ->
            noteViewModel.handleEvent(NoteDetailEvent.UpdateNoteCardColor(hex))
            simulate_content_card_view.setCardBackgroundColor(hex)
            simulate_title_card_view.setCardBackgroundColor(hex)
        }
    }

    private fun setUpViewListener() {
        tx_update_note.setOnClickListener(this@NoteDetailView)
        im_add_email.setOnClickListener(this@NoteDetailView)
        ed_note_content.beginActionEnabledTextViewWatcher(tx_update_note)
        ed_note_content.beginDoubleValueTextViewWatcher(sim_lbl_content)
        ed_note_title.beginDoubleValueTextViewWatcher(sim_lbl_title)
        simulate_content_card_view.setClickListenerWithViewDelayEnabled {
            updateSheetToExpandingState()
        }
        sim_img_option_menu.setClickListenerWithViewDelayEnabled {
            val popupMenu = PopupMenu(
                context, sim_img_option_menu
            )
            popupMenu.baseNoteInflationMenu(
                content = ed_note_content.text?.trim().toString()
            )
        }
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (noteViewModel.isBottomSheetAtExpandingState()) updateSheetToHideState()
            else findNavController().popBackStack()
        }
    }

    private fun PopupMenu.baseNoteInflationMenu(
        content: String,
    ) {
        this@baseNoteInflationMenu.inflate(R.menu.menu_note_detail)
        this@baseNoteInflationMenu.menu.findItem(R.id.delete_menu)
            .setTitleColor(android.graphics.Color.RED)
        this@baseNoteInflationMenu.setOnMenuItemClickListener { item ->
            when (item?.itemId) {
                R.id.copy_menu -> context?.actionCopyText(content)
                R.id.share_text_menu -> context?.actionShareText(content)
                R.id.open_link_menu -> context?.actionOpenUrl(content)
                R.id.delete_menu -> noteViewModel.handleEvent(NoteDetailEvent.DeleteNote)
            }
            false
        }
        this@baseNoteInflationMenu.show()
    }

    private fun NoteViewModel.startObserving() {
        error.observe(viewLifecycleOwner) {
            activity?.makeToast(it.asString(context))
        }
        loading.observe(viewLifecycleOwner) {
            if (it) commView.showProgress()
            else commView.hideProgress()
        }
        bottomSheetViewState.observe(viewLifecycleOwner) {
            noteBottomSheetBehavior.state = it
        }
        updated.observe(viewLifecycleOwner) {
            if (it) navigateToNoteListView()
        }
        deleted.observe(viewLifecycleOwner) {
            if (it) navigateToNoteListView()
        }
        actionLoginAttempt.observe(viewLifecycleOwner) {
            navigateToLoginView()
            (activity as NoteActivity).finish()
        }
        actionSharingProcessAttempt.observe(viewLifecycleOwner) {
            context?.actionDisplayConfirmationDialog(
                R.string.share_icon_hint, R.string.Skip
            ) {
                ed_user_email.setText("")
                updateNote(
                    content = ed_note_content.text?.trim().toString(),
                    title = ed_note_title.text?.trim().toString(),
                    email = ""
                )
            }
        }
        textSharedNoteState.observe(viewLifecycleOwner) {
            sim_lbl_owner.text = it.asString(context)
        }
        iconSharedNoteState.observe(viewLifecycleOwner) {
            im_add_email.setImageResource(it)
        }
        noteItemSharedState.observe(viewLifecycleOwner) {
            if (it) {
                im_add_email.isClickable = false
                ed_user_email.disableEditEmailListener()
            }
        }
        actionInviteAttempt.observe(viewLifecycleOwner) {
            context?.let {
                it.actionDisplayConfirmationDialog(
                    R.string.user_doesn_not_exist, R.string.Invite
                ) {
                    it.actionShareText(getString(R.string.invite_text))
                }
            }
            ed_user_email.setText("")
        }
        note.observe(viewLifecycleOwner) {
            ed_note_content.text = it.content.toEditable()
            ed_note_title.text = it.title.toEditable()
            simulate_content_card_view.setCardBackgroundColor(hexColor = it.hexCardColor)
            simulate_title_card_view.setCardBackgroundColor(hexColor = it.hexCardColor)
            if (it.content == "") {
                sim_lbl_content.text = NOTE_DEF_INPUT
                sim_lbl_title.text = NOTE_DEF_INPUT
            }
        }
    }

    private fun EditText.disableEditEmailListener() {
        isFocusable = false
        setText(R.string.shared)
        paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
    }

    private fun updateNote(content: String, title: String, email: String) {
        noteViewModel.handleEvent(
            NoteDetailEvent.OnUpdateNoteClicked(
                content = content,
                title = title,
                email = email
            )
        )
    }
}
