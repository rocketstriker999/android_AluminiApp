package com.sanatanshilpisanstha.ui.group

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import com.sanatanshilpisanstha.data.entity.City
import com.sanatanshilpisanstha.data.entity.group.ChatDateItem
import com.sanatanshilpisanstha.data.entity.group.ChatListItem
import com.sanatanshilpisanstha.data.entity.group.message.LikeCommentModel
import com.sanatanshilpisanstha.data.entity.group.message.MMessage
import com.sanatanshilpisanstha.databinding.ActivityLikesCommentBinding
import com.sanatanshilpisanstha.remote.APIResult
import com.sanatanshilpisanstha.repository.AccountRepository
import com.sanatanshilpisanstha.ui.adapter.CommentAdapter
import com.sanatanshilpisanstha.ui.adapter.LikesAdapter
import com.sanatanshilpisanstha.utility.Constant.MessageId
import com.sanatanshilpisanstha.utility.Utilities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class LikesCommentActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityLikesCommentBinding

    lateinit var likesAdapter: LikesAdapter
    lateinit var commentAdapter: CommentAdapter

    private val parentJob = Job()
    private val coroutineContext: CoroutineContext get() = parentJob + Dispatchers.Main
    private val scope = CoroutineScope(coroutineContext)
    private lateinit var accountRepository: AccountRepository
    lateinit var likesList: ArrayList<LikeCommentModel.Data.Like>
    lateinit var commentsList: ArrayList<LikeCommentModel.Data.Comment>
    var messageId =""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityLikesCommentBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        accountRepository = AccountRepository(this)

        supportActionBar?.hide()
        linear()

    }

    private fun linear() {
        binding.blankLinear1.setOnClickListener(this)
        binding.blankLinear2.setOnClickListener(this)
        binding.ivBack.setOnClickListener(this)
        messageId = intent.getStringExtra(MessageId).toString()
        likesList = ArrayList()
        commentsList = ArrayList()
        getLikeCommentList()
        }

    override fun onClick(v: View?) {
        when (v) {
            binding.blankLinear1 -> {

                binding.likesLinear.visibility = View.VISIBLE
                binding.commentLinear.visibility = View.GONE
                binding.blankLinear2.visibility = View.VISIBLE
                binding.blankLinear1.visibility = View.GONE
                setLikesAdapter()
            }
            binding.blankLinear2 -> {
                binding.likesLinear.visibility = View.GONE
                binding.commentLinear.visibility = View.VISIBLE
                binding.blankLinear2.visibility = View.GONE
                binding.blankLinear1.visibility = View.VISIBLE

                setCommentsAdapter()
            }
            binding.ivBack -> {
                finish();
            }
        }
    }

    private fun setLikesAdapter() {

        likesAdapter = LikesAdapter(likesList,this)

        binding.likeCommentList.apply {
            adapter = likesAdapter
        }

    }
    private fun setCommentsAdapter() {

        commentAdapter = CommentAdapter(commentsList,this)

        binding.likeCommentList.apply {
            adapter = commentAdapter
        }

    }


    fun getLikeCommentList() {
        scope.launch {
            accountRepository.getLikeComments(
                messageId
            ) {
                when (it) {
                    is APIResult.Success -> {

                        likesList = it.data.data?.likes as ArrayList<LikeCommentModel.Data.Like>;
                        commentsList = it.data.data?.comments as ArrayList<LikeCommentModel.Data.Comment>;


                        setLikesAdapter()
                    }

                    is APIResult.Failure -> {
                        Utilities.showErrorSnackBar(binding.likeLinear, it.message.toString())
                    }

                    else -> {}
                }
            }
        }
    }

}