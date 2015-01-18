package blokusgame.mi.android.hazi.blokus;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;

import View.BlockView;
import View.BoardTouchListener;
import View.BoardView;
import blokusgame.mi.android.hazi.blokus.GameLogic.Block;
import blokusgame.mi.android.hazi.blokus.GameLogic.Map;
import blokusgame.mi.android.hazi.blokus.GameLogic.Player;
import blokusgame.mi.android.hazi.blokus.GameLogic.PlayerAlgorithm;
import blokusgame.mi.android.hazi.blokus.GameLogic.PlayerHuman;
import blokusgame.mi.android.hazi.blokus.GameLogic.Point;

import static android.view.View.OnClickListener;

public class MainActivity extends Activity implements BoardTouchListener {
    private Map map = Map.getInstance();
    private Player player1 = new PlayerHuman(1);
    private PlayerAlgorithm player2 = new PlayerAlgorithm(2);

    private LinearLayout horizontal_scroll;
    private LinearLayout rotations_layout;
    private SlidingUpPanelLayout slidingUpLayout;
    private BoardView boardView;
    private BlockViewOnClickListener blockClickListener = new BlockViewOnClickListener();
    private RotatedBlockViewOnClickListener rotatedBlockClickListener = new RotatedBlockViewOnClickListener();

    private ImageView choosenBlockView = null;
    private Point choosenPoint = null;
    private Block choosenBlock = null;
    private int blockIndex = -1;

    private boolean isHumanFirst = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        boardView = (BoardView) findViewById(R.id.boardView);
        boardView.setWasTouchedListener(this);

        horizontal_scroll = (LinearLayout) findViewById(R.id.horizontal_layout);
        rotations_layout = (LinearLayout) findViewById(R.id.rotations_layout);

        slidingUpLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        slidingUpLayout.setCoveredFadeColor(Color.TRANSPARENT);
        slidingUpLayout.setPanelHeight((int) getResources().getDimension(R.dimen.collapsed_height));

        reset();

        // show the menu
        showStartMenuDialog();

        Button btnStep = (Button) findViewById(R.id.btnStep);
        btnStep.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(step()){
                    AIstep();
                    // if the player is out of moves, finish
                    if(player1.getCorners().isEmpty()){
                        while(!player2.getCorners().isEmpty()){
                            AIstep();
                        }
                    }
                }

                boardView.invalidate();
                if (player1.getCorners().isEmpty() && player2.getCorners().isEmpty()) {
                    Log.e("MAIN", "Game ended");
                    Point results = new Point(0,0);
                    for(int i=0; i<map.getLineSize()*map.getLineSize(); ++i){
                        if(map.getCell(i)==player1.getColor()){
                            ++results.x;
                        } else if(map.getCell(i)==player2.getColor()){
                            ++results.y;
                        }
                    }
                    showResultDialog(results);
                    Log.e("POINTS: ", "Human: "+String.valueOf(results.x)+" AI: "+ String.valueOf(results.y));
                }
            }
        });
        Button btnMenu = (Button) findViewById(R.id.btn_menu);
        btnMenu.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showMenuDialog();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // if app restarts we dont want the previous game (strange bugfix)
        reset();
    }

    private void reset() {
        map.reset();
        player1 = new PlayerHuman(1);
        setPlayer(player1);
        player2 = new PlayerAlgorithm(2);
        player2.setEnemy(player1);
        boardView.setCorners(player1.getCorners());
        boardView.setOverlayBlock(null, null);
        boardView.invalidate();
        rotations_layout.removeAllViews();
    }

    private boolean step() {
        if (choosenBlock != null && choosenPoint != null) {
            // corners not empty = there is still place to step
            if (!player1.getCorners().isEmpty() && player1.placeBlock(choosenBlock, choosenPoint)) {
                boardView.setOverlayBlock(null, null);
                horizontal_scroll.removeView(choosenBlockView);
                choosenBlock = null;
                choosenPoint = null;
                choosenBlockView = null;
                rotations_layout.removeAllViews();
                boardView.invalidate();
            } else{
                return false;
            }
        } else{
            Toast.makeText(getApplicationContext(), "Block, or cell not selected", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    private boolean AIstep(){
        // AI player only steps is player did actually step
        if (!player2.getCorners().isEmpty()) {
            // redundancy
            player2.nextStep();
            boardView.setCorners(player1.getCorners());
            slidingUpLayout.expandPanel();
        } else{
            return false;
        }

        return true;
    }
    void setPlayer(Player player){
        ArrayList<Block> blocks = new ArrayList<Block>();
        horizontal_scroll.removeAllViews();
        for(int i=0; i<21; ++i){
            Block block = player.getBlock(i);
            if(block!=null){
                blocks.add(block);
            }
            slidingUpLayout.expandPanel();
        }

        boardView.setCorners(player1.getCorners());

        for (Block block : blocks) {
            ImageView imageView = new ImageView(this);
            imageView.setBackgroundResource(block.getImageId());
            imageView.setId(block.getId());
            imageView.setOnClickListener(blockClickListener);
            horizontal_scroll.addView(imageView);
        }
    }

    @Override
    public void onBoardTouched(int x, int y) {
        choosenPoint = new Point(x,y);
        Log.e("Cell coordinates:", String.valueOf(x)+" "+String.valueOf(y));
        if(choosenBlockView!=null) {
            boardView.setOverlayBlock(choosenBlock, choosenPoint);
            boardView.invalidate();
        }
    }

    // a felcsuszo ablakon a block kepeket kezeli
    public class BlockViewOnClickListener implements OnClickListener {
        @Override
        public void onClick(View view) {
            blockIndex = view.getId();
            try {
                choosenBlock = player1.getBlock(view.getId());
                boardView.setOverlayBlock(choosenBlock, choosenPoint);
                boardView.invalidate();
                // draw rotations
                rotations_layout.removeAllViews();
                ArrayList<Block> rotations = choosenBlock.getRotations();
                for(Block b:rotations){
                    BlockView bView = new BlockView(getApplicationContext());
                    bView.setBlock(b);
                    bView.setOnClickListener(rotatedBlockClickListener);
                    rotations_layout.addView(bView);
                }
                rotations_layout.invalidate();
            }catch (NumberFormatException ex){
                Toast.makeText(getApplicationContext(), "Set the coordinates too", Toast.LENGTH_SHORT).show();
            }
            choosenBlockView = (ImageView) view;
            slidingUpLayout.collapsePanel();
        }
    }
    public class RotatedBlockViewOnClickListener implements OnClickListener{

        @Override
        public void onClick(View view) {
            Block block = ((BlockView)view).getBlock();
            blockIndex = block.getId();
            choosenBlock = block;
            boardView.setOverlayBlock(choosenBlock, choosenPoint);
            boardView.invalidate();
        }
    }
    private void showStartMenuDialog() {
        // custom dialog
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.game_start);
        dialog.setTitle("Menu");
        dialog.setCanceledOnTouchOutside(false);

        // set the custom dialog components - text, image and button
        Button playerStart = (Button) dialog.findViewById(R.id.btn_player_start);
        Button AIStart = (Button) dialog.findViewById(R.id.btn_ai_start);



        playerStart.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isHumanFirst = true;
                reset();
                dialog.dismiss();
            }
        });
        AIStart.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isHumanFirst = false;
                reset();
                // have to do it here unfortunately
                AIstep();
                dialog.dismiss();
                boardView.invalidate();
            }
        });
        dialog.show();
    }

    private void showMenuDialog(){
        // custom dialog
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.game_menu);
        dialog.setTitle("Menu");
        dialog.setCanceledOnTouchOutside(false);

        // set the custom dialog components - text, image and button
        Button resume = (Button) dialog.findViewById(R.id.btn_resume);
        Button playerStart = (Button) dialog.findViewById(R.id.btn_player_start);
        Button AIStart = (Button) dialog.findViewById(R.id.btn_ai_start);

        resume.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                 dialog.dismiss();
            }
        });

        playerStart.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isHumanFirst = true;
                reset();
                dialog.dismiss();
            }
        });
        AIStart.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isHumanFirst = false;
                reset();
                // have to do it here unfortunately
                AIstep();
                dialog.dismiss();
                boardView.invalidate();
            }
        });
        dialog.show();
    }
    private void showResultDialog(Point results){
        // custom dialog
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.game_end);
        dialog.setTitle("GameOver");
        dialog.setCanceledOnTouchOutside(false);

        // set the custom dialog components - text, image and button
        TextView resultText = (TextView) dialog.findViewById(R.id.txt_result);
        TextView firstResult = (TextView) dialog.findViewById(R.id.txt_first_result);
        TextView secondResult = (TextView) dialog.findViewById(R.id.txt_second_result);

        if(results.x==results.y) {
            resultText.setText("Its a draw");
            firstResult.setText("Your points: "+String.valueOf(results.x));
            secondResult.setText("Opponent's points: "+String.valueOf(results.y));
        } else {
            if(results.x>results.y) {
                resultText.setText("You won!!!");
                firstResult.setText("Your points: "+String.valueOf(results.x));
                secondResult.setText("Opponent's points: "+String.valueOf(results.y));
            } else {
                resultText.setText("You lost...");
                firstResult.setText("Opponent's points: "+String.valueOf(results.y));
                secondResult.setText("Your points: "+String.valueOf(results.x));
            }
        }

        Button dialogButton = (Button) dialog.findViewById(R.id.btn_reset);
        // if button is clicked, close the custom dialog
        dialogButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                isHumanFirst = !isHumanFirst;
                reset();
            }
        });

        dialog.show();
    }
}
