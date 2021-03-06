import com.sun.corba.se.spi.logging.CORBALogDomains;
import sun.plugin2.gluegen.runtime.CPU;

import java.util.ArrayList;

public class Board {
    public Checker[][] board = new Checker[8][8];
    private int remaining_moves;
    public int value;


    public Board()
    {
        value = 0;
        for (int i = 0; i < 8; i++)
        {
            for (int j = 0; j < 8; j++)
            {
                board[i][j] = new Checker(i, j);
            }
        }
        remaining_moves = 60;
        board[3][3].setColor(Color.Black);
        board[3][4].setColor(Color.White);
        board[4][3].setColor(Color.White);
        board[4][4].setColor(Color.Black);
    }

    public Board(Board board) {
        this.remaining_moves = board.remaining_moves;
        this.value = board.value;
        for (int i = 0; i < 8; i++)
        {
            for (int j = 0; j < 8; j++)
            {
                this.board[i][j] = new Checker(board.board[i][j].getColor(), board.board[i][j].position);
            }
        }

    }

    public void printUI() {

        System.out.println("  1 2 3 4 5 6 7 8");
        String[] letters = {"A", "B", "C", "D", "E", "F", "G", "H"};
        for (int i = 0; i < 8; i++)
        {
            System.out.print(letters[i] + " ");
            for (int j = 0; j < 8; j++)
            {
                System.out.print(board[j][i].getItem() + " ");
            }
            System.out.println();
        }
    }

    public Checker getChecker(int x, int y) {
        return board[x][y];
    }

    public ArrayList<Move> avaliableMoves(int curr_color)
    {
        ArrayList<Move> a = new ArrayList<>();
        for (int i = 0; i < 8; i++)
        {
            for (int j = 0; j < 8; j++)
            {
                Checker current = board[i][j];
                if (current.getColor() == Color.Empty)
                {
                    for (int x = -1; x <= 1; x++)
                    {
                        for (int y = -1; y <= 1; y++)
                        {
                            if (x != 0 || y != 0)
                            {
                                try
                                {
                                    //Find a neighbor
                                    Checker neighbor = getChecker(current.getX() + x, current.getY() + y);
                                    //If this neighbor has the opponent's color then..
                                    if (neighbor.getColor() == 3 - curr_color)
                                    {
                                        int counter = 0;
                                        //..we start checking towards that direction while we don't find an Empty cell
                                        while (neighbor.getColor() != Color.Empty)
                                        {
                                            //My new neighbor is the neighbor's neighbor
                                            neighbor = getChecker(neighbor.getX() + x, neighbor.getY() + y);
                                            //Count the old neighbor
                                            counter++;
                                            //If the new neighbor is mine then we have a potential move
                                            if (neighbor.getColor() == curr_color)
                                            {
                                                //Check if this move is already in my move list
                                                boolean found = false;
                                                for (Move move : a)
                                                {
                                                    //It is! So I have to add this new vector to the move
                                                    if (move.getX() == current.getX() && move.getY() == current.getY())
                                                    {
                                                        move.insertDir(new int[]{x, y, counter});
                                                        found = true;
                                                        break;
                                                    }
                                                }
                                                //It isn't so I add a new move
                                                if (!found)
                                                {
                                                    Move temp_move = new Move(current.getX(), current.getY());
                                                    temp_move.insertDir(new int[]{x, y, counter});
                                                    a.add(temp_move);
                                                }
                                            }
                                        }
                                    }
                                }
                                //In case we search out of bound just ignore the error and move on
                                catch (Exception e)
                                {
                                    continue;
                                }
                            }
                        }
                    }
                }

            }
        }
        return a;
    }

    public ArrayList<Board> getChildren(int curr_color)
    {
        ArrayList<Board> children = new ArrayList<Board>();

        for (Move m : avaliableMoves(curr_color))
        {
            children.add(makeMove(new Board(this), m, curr_color));
        }
        return children;
    }

    public Board makeMove(Board temp_board, Move move, int currColor)
    {
        temp_board.getChecker(move.getX(), move.getY()).setColor(currColor);
        temp_board.remaining_moves--;

        for (int[] i : move.getDirections())
        {
            int vx = move.getX() + i[0];
            int vy = move.getY() + i[1];

            for (int j = 0; j < i[2]; j++)
            {
                temp_board.getChecker(vx, vy).setColor(currColor);

                vx += i[0];
                vy += i[1];
            }
        }
        temp_board.evaluate();
        return temp_board;
    }

    public boolean isFinal()
    {
        if (remaining_moves <=0) {
            return true;
        }
        return false;
    }

    public void evaluate()
    {
        value = 0;
        int[][] board_value =
                {
                        {20, -3, 11, 8,  8, 11, -3, 20},
                        {-3, -7, -4, 1,  1, -4, -7, -3},
                        {11, -4, 2,  2,  2,  2, -4, 11},
                        { 8,  1, 2, -3, -3,  2,  1,  8},
                        { 8,  1, 2, -3, -3,  2,  1,  8},
                        {11, -4, 2,  2,  2,  2, -4, 11},
                        {-3, -7, -4, 1,  1, -4, -7, -3},
                        {20, -3, 11, 8,  8, 11, -3, 20}
                };

        int b = 0, w = 0;
        for(int x = 0; x < 8; x++)
        {
            for(int y = 0; y < 8; y++)
            {
                if(board[x][y].getColor() == Color.Black)
                {
                    b += board_value[x][y];
                }
                else if(board[x][y].getColor() == Color.White)
                {
                    w += board_value[x][y];
                }
            }
        }
        try
        {
            value = (b - w) / (b + w);
        }
        catch (Exception e)
        {
            value = 0;
        }

        b += avaliableMoves(Color.Black).size();
        w += avaliableMoves(Color.White).size();

        try
        {
            value += (b - w) / (b + w);
        }
        catch (Exception e)
        {
            value = 0;
        }
    }

    public String getScore() {
        int whites=0, blacks=0;
        for (int i=0;i<8;i++) for (int j=0;j<8;j++) {
            if (board[i][j].getColor() == Color.White) whites++;
            if (board[i][j].getColor() == Color.Black) blacks++;
        }

        return "Whites: " + whites + " Blacks: " + blacks;
    }
}

