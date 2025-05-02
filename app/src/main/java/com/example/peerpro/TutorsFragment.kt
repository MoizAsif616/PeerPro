package com.example.peerpro

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.peerpro.databinding.FragmentTutorsBinding
import com.example.peerpro.databinding.TutorCardBinding

class TutorsFragment : Fragment() {

  private var _binding: FragmentTutorsBinding? = null
  private val binding get() = _binding!!

  data class Card(
    val imageRes: Any,
    val name: String,
    val rollNumber: String,
    val subject: String,
    val preferredGender: String,
    val timeWindow: String,
    val sessionType: String,
    val availableDays: String,
    val cost: String,
    val costType: String
  )

  private inner class TutorsAdapter(private val tutors: List<Card>, private val onTutorClick: (Card) -> Unit
  ) :
    RecyclerView.Adapter<TutorsAdapter.TutorViewHolder>() {

    // Cached size calculations
    private var itemHeight: Int = 0
    private var itemWidth: Int = 0
    private var itemMarginBottom: Int = 0
    private var dateSize: Float = 0f
    private var textSizeMedium: Float = 0f
    private var textSizeSmall: Float = 0f
    private var imageSize: Int = 0

    inner class TutorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
      private val binding = TutorCardBinding.bind(itemView)


      fun bind(tutor: Card) {
        // Apply cached sizes
        binding.mainContainer.layoutParams = LinearLayout.LayoutParams(
          itemWidth,
          itemHeight
        ).apply {
          bottomMargin = itemMarginBottom
        }
        itemView.setOnClickListener {
          onTutorClick(tutor)
        }
        binding.tutorImage.layoutParams.width = (1.01 * imageSize).toInt()
        binding.tutorImage.layoutParams.height = imageSize

        (binding.tutorName.layoutParams as ViewGroup.MarginLayoutParams).topMargin = (itemHeight * 0.005).toInt()
        (binding.tutorRoll.layoutParams as ViewGroup.MarginLayoutParams).topMargin = -(itemHeight * 0.005).toInt()

        // Set text sizes
        binding.tutorName.textSize = textSizeSmall
        binding.tutorRoll.textSize = textSizeSmall
        binding.tutorSubject.textSize = textSizeMedium
        binding.genderLabel.textSize = textSizeSmall
        binding.tutorGender.textSize = textSizeSmall
        binding.sessionTypeLabel.textSize = textSizeSmall
        binding.tutorSessionType.textSize = textSizeSmall
        binding.availableDaysLabel.textSize = textSizeSmall
        binding.tutorAvailableDays.textSize = textSizeSmall
        binding.timeWindowLabel.textSize = textSizeSmall
        binding.tutorTimeWindow.textSize = textSizeSmall
        binding.costLabel.textSize = textSizeSmall
        binding.tutorCost.textSize = textSizeSmall
        binding.tutorDate.textSize = dateSize

        // Bind data
        binding.tutorName.text = tutor.name
        binding.tutorRoll.text = tutor.rollNumber
        binding.tutorSubject.text = tutor.subject
        binding.tutorGender.text = tutor.preferredGender
        binding.tutorSessionType.text = tutor.sessionType
        binding.tutorAvailableDays.text = tutor.availableDays
        binding.tutorTimeWindow.text = tutor.timeWindow

        if (tutor.cost == "Rs. 0") {
          binding.tutorCost.text = "Free"
          binding.costLabel.visibility = View.GONE
        } else {
          binding.tutorCost.text = tutor.cost
          binding.costLabel.text = tutor.costType
        }
        // Set date if needed
      }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TutorViewHolder {
      // Calculate sizes only once when first ViewHolder is created
      if (itemHeight == 0) {
        val displayMetrics = DisplayMetrics()
        val windowManager = parent.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        itemHeight = (screenHeight * 0.33).toInt()
        itemWidth = (screenWidth * 0.47).toInt()
        itemMarginBottom = (screenWidth * 0.02).toInt()

        dateSize = (itemHeight * 0.021f)
        textSizeMedium = (itemHeight * 0.03f)
        textSizeSmall = (itemHeight * 0.027f)  //0.0173f
        imageSize = (itemHeight * 0.2).toInt()
      }

      val view = LayoutInflater.from(parent.context)
        .inflate(R.layout.tutor_card, parent, false)
      return TutorViewHolder(view)
    }

    override fun onBindViewHolder(holder: TutorViewHolder, position: Int) {
      holder.bind(tutors[position])
    }

    override fun getItemCount(): Int = tutors.size
  }

  private inner class HorizontalSpacingDecoration : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
      val spacing = (resources.displayMetrics.widthPixels * 0.01).toInt() // 1% spacing
      if (parent.getChildAdapterPosition(view) % 2 == 0) outRect.right = spacing else outRect.left = spacing
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    _binding = FragmentTutorsBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    // Sample data
    val tutors = listOf(
      Card(
        imageRes = R.color.black,
        name = "Moiz Asif",
        rollNumber = "l22-6720",
        subject = "Django Rest Framework and MVT",
        preferredGender = "Male",
        timeWindow = "Evening",
        sessionType = "Online",
        availableDays = "Mon, Wed, Fri",
        cost = "Rs. 5000",
        costType = "One-time"
      ),
      Card(
        imageRes = R.color.teal_200,
        name = "Huria Ali",
        rollNumber = "l22-6629",
        subject = "ReactNative and Expo",
        preferredGender = "Female",
        timeWindow = "Evening",
        sessionType = "Online",
        availableDays = "Mon, Wed, Fri",
        cost = "Rs. 0",
        costType = "One-time"
      ),
      Card(
        imageRes = R.color.teal_200,
        name = "saminiftikhar_099",
        rollNumber = "l22-6629",
        subject = "ReactNative and Expo",
        preferredGender = "Any",
        timeWindow = "Evening",
        sessionType = "Online",
        availableDays = "Mon, Wed, Fri",
        cost = "Rs. 500",
        costType = "Per session"
      ),
    )
    binding.tutorsCardsRecyclerView.layoutManager = GridLayoutManager(context, 2)
    binding.tutorsCardsRecyclerView.adapter = TutorsAdapter(tutors){ tutor ->
      displayTutorDialog(tutor)
    }
    binding.tutorsCardsRecyclerView.addItemDecoration(HorizontalSpacingDecoration())

  }

  fun searchTutors(query: String) {
    // Implement search functionality here
    // For example, filter a list of tutors based on the query
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
  @SuppressLint("SetTextI18n")
  private fun displayTutorDialog(tutor: Card) {
    val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.tutor_details, null)
    val dialog = android.app.AlertDialog.Builder(requireContext())
      .setView(dialogView)
      .setCancelable(true)
      .create()

    val metrics = Resources.getSystem().displayMetrics
    val screenWidth = metrics.widthPixels
    val screenHeight = metrics.heightPixels
    val windowHeight = (screenHeight * 0.6).toInt()
    val textSizeMedium = (windowHeight * 0.017f)
    val textSizeSmall = (windowHeight * 0.013f)
    val textSizeExtraSmall = (windowHeight * 0.01f)
    val textSizeLarge = (windowHeight * 0.02f)
    dialog.setOnShowListener {
      val window = dialog.window
      window?.setBackgroundDrawableResource(android.R.color.transparent)
      window?.setLayout((screenWidth * 0.75).toInt(), (screenHeight * 0.6).toInt())
      window?.setGravity(Gravity.CENTER)
      dialogView.minimumHeight = windowHeight

    }

    // Assign values to your text views (as you already did)
    val name = dialogView.findViewById<TextView>(R.id.name)
    val roll = dialogView.findViewById<TextView>(R.id.rollno)
    val subject = dialogView.findViewById<TextView>(R.id.subject)
    val gender = dialogView.findViewById<TextView>(R.id.tgender)
    val session = dialogView.findViewById<TextView>(R.id.type)
    val days = dialogView.findViewById<TextView>(R.id.days)
    val costType = dialogView.findViewById<TextView>(R.id.costType)
    val timeWindow = dialogView.findViewById<TextView>(R.id.timeWindow)
    val cost = dialogView.findViewById<TextView>(R.id.cost)
    val description = dialogView.findViewById<TextView>(R.id.description)
    val genderLabel = dialogView.findViewById<TextView>(R.id.genderLabel)
    val sessionTypeLabel = dialogView.findViewById<TextView>(R.id.sessionTypeLabel)
    val availableDaysLabel = dialogView.findViewById<TextView>(R.id.availableDaysLabel)
    val timeWindowLabel = dialogView.findViewById<TextView>(R.id.timeLabel)
    val text = dialogView.findViewById<TextView>(R.id.text)
    val requestBtn= dialogView.findViewById<TextView>(R.id.requestButton)

    requestBtn.layoutParams.height = (windowHeight * 0.08f).toInt()
    name.text = tutor.name
    name.textSize = textSizeLarge
    roll.text = tutor.rollNumber
    roll.textSize = textSizeLarge
    subject.text = tutor.subject
    subject.textSize = textSizeMedium
    gender.text = tutor.preferredGender
    gender.textSize = textSizeSmall
    session.text = tutor.sessionType
    session.textSize = textSizeSmall
    days.text = tutor.availableDays
    days.textSize = textSizeSmall
    timeWindow.text = tutor.timeWindow
    timeWindow.textSize = textSizeSmall
    costType.text = tutor.costType
    costType.textSize = textSizeSmall
    cost.text = if (tutor.cost == "Rs. 0") "Free" else tutor.cost
    cost.textSize = textSizeSmall
    description.textSize = textSizeSmall
    sessionTypeLabel.textSize = textSizeSmall
    availableDaysLabel.textSize = textSizeSmall
    timeWindowLabel.textSize = textSizeSmall
    genderLabel.textSize = textSizeSmall
    text.textSize = textSizeMedium
    requestBtn.textSize = textSizeExtraSmall
    dialog.show()
  }


}