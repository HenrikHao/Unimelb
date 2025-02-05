package src;

public class TetrisComponents {
    // AUTO GENERATED - do not modify//GEN-BEGIN:variables
    public void initComponents(AbstractTetris abstractTetris) {
        abstractTetris.jPanel1 = new javax.swing.JPanel();
        abstractTetris.gameGrid2 = new ch.aplu.jgamegrid.GameGrid();
        abstractTetris.startBtn = new javax.swing.JButton();
        abstractTetris.jPanel2 = new javax.swing.JPanel();
        abstractTetris.gameGrid1 = new ch.aplu.jgamegrid.GameGrid();
        abstractTetris.jPanel3 = new javax.swing.JPanel();
        abstractTetris.jScrollPane1 = new javax.swing.JScrollPane();
        abstractTetris.jTextArea1 = new javax.swing.JTextArea();
        abstractTetris.jPanel4 = new javax.swing.JPanel();
        abstractTetris.scoreText = new javax.swing.JTextField();

        abstractTetris.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        abstractTetris.jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Next TetrisBlock"));

        abstractTetris.gameGrid2.setCellSize(20);
        abstractTetris.gameGrid2.setNbHorzCells(6);
        abstractTetris.gameGrid2.setNbVertCells(4);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(abstractTetris.jPanel1);
        abstractTetris.jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(28, 28, 28)
                                .addComponent(abstractTetris.gameGrid2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(28, 28, 28))
        );
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(abstractTetris.gameGrid2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(20, Short.MAX_VALUE))
        );

        abstractTetris.startBtn.setText("Start");
        abstractTetris.startBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                abstractTetris.startBtnActionPerformed(evt);
            }
        });

        abstractTetris.jPanel2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        abstractTetris.gameGrid1.setCellSize(20);
        abstractTetris.gameGrid1.setGridColor(new java.awt.Color(255, 3, 0));
        abstractTetris.gameGrid1.setNbVertCells(30);
        abstractTetris.gameGrid1.setNbHorzCells(15);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(abstractTetris.jPanel2);
        abstractTetris.jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(abstractTetris.gameGrid1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(abstractTetris.gameGrid1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        abstractTetris.jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Instructions"));

        abstractTetris.jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        abstractTetris.jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        abstractTetris.jTextArea1.setBackground(new java.awt.Color(102, 255, 255));
        abstractTetris.jTextArea1.setColumns(20);
        abstractTetris.jTextArea1.setEditable(false);
        abstractTetris.jTextArea1.setFont(new java.awt.Font("Arial", 0, 10)); // NOI18N
        abstractTetris.jTextArea1.setRows(5);
        abstractTetris.jTextArea1.setText(" Welcome to the JGameGrid Tetris! \n\n Cursor keys:\n    Left/right:  Move tetrisBlock\n    Up:  Rotate tetrisBlock\n    Down:  Drop tetrisBlock\n  \n When a  line  is complete, it is removed \n and you earn one point, but the speed\n of  the tetrisBlocks  increases  more and\n more...\n       \n       Good luck!");
        abstractTetris.jTextArea1.setDisabledTextColor(new java.awt.Color(102, 102, 255));
        abstractTetris.jTextArea1.setFocusable(false);
        abstractTetris.jTextArea1.setRequestFocusEnabled(false);
        abstractTetris.jScrollPane1.setViewportView(abstractTetris.jTextArea1);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(abstractTetris.jPanel3);
        abstractTetris.jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
                jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(abstractTetris.jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 203, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(12, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
                jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(abstractTetris.jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        abstractTetris.jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Score"));

        abstractTetris.scoreText.setBackground(new java.awt.Color(255, 102, 102));
        abstractTetris.scoreText.setEditable(false);
        abstractTetris.scoreText.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        abstractTetris.scoreText.setText("0");
        abstractTetris.scoreText.setAutoscrolls(false);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(abstractTetris.jPanel4);
        abstractTetris.jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
                jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addComponent(abstractTetris.scoreText, javax.swing.GroupLayout.PREFERRED_SIZE, 183, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(24, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
                jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(abstractTetris.scoreText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(abstractTetris.getContentPane());
        abstractTetris.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(abstractTetris.jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addGroup(layout.createSequentialGroup()
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                        .addComponent(abstractTetris.jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 237, Short.MAX_VALUE)
                                                        .addComponent(abstractTetris.jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(abstractTetris.jPanel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                .addContainerGap())
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(abstractTetris.startBtn)
                                                .addGap(94, 94, 94))))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(abstractTetris.jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                                .addComponent(abstractTetris.jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(abstractTetris.jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(abstractTetris.jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addComponent(abstractTetris.startBtn)))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        abstractTetris.pack();
    }// </editor-fold>//GEN-END:initComponents
}
