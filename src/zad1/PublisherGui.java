package zad1;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class PublisherGui extends JFrame{
    public Publisher publisher;
    public PublisherGui(Publisher publisher){
        this.publisher = publisher;
        new JFrame("Publisher");
        this.setTitle("Publisher");

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        JPanel addTopicPanel = new JPanel();
        addTopicPanel.setLayout(new GridBagLayout());
        Border addTopicBorder = BorderFactory.createTitledBorder("Dodaj temat");
        addTopicPanel.setBorder(addTopicBorder);

        JTextField topic = new JTextField();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 0.5;
        constraints.weighty = 0.5;
        constraints.gridy = 0;

        addTopicPanel.add(topic,constraints);

        JButton addTopicButton = new JButton("Dodaj temat");
        constraints.gridy = 1;

        addTopicPanel.add(addTopicButton,constraints);

        constraints.gridy = 0;

        mainPanel.add(addTopicPanel,constraints);

        JPanel removeTopicPanel = new JPanel();
        removeTopicPanel.setLayout(new GridBagLayout());

        JComboBox<String> topicList = new JComboBox();
        Border topicListBorder = BorderFactory.createTitledBorder("Tematy");
        topicList.setBorder(topicListBorder);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridy = 0;
        constraints.gridx = 0;

        removeTopicPanel.add(topicList,constraints);

        JButton removeTopicButton = new JButton("Usuń temat");
        constraints.gridy = 0;
        constraints.gridx = 1;

        removeTopicPanel.add(removeTopicButton,constraints);

        constraints.gridy = 1;
        constraints.gridx = 0;

        mainPanel.add(removeTopicPanel,constraints);

        JPanel newsPanel = new JPanel();
        newsPanel.setLayout(new GridBagLayout());

        Border newsBorder = BorderFactory.createTitledBorder("Wyślij news'a");
        newsPanel.setBorder(newsBorder);

        JTextArea newsText = new JTextArea(10,50);
        newsText.setLineWrap(true);
        newsText.setWrapStyleWord(true);

        newsPanel.add(new JScrollPane(newsText));

        JButton newsButton = new JButton("News");

        newsPanel.add(newsButton);

        constraints.gridy = 2;
        constraints.gridx = 0;
        mainPanel.add(newsPanel,constraints);

        addTopicButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!topic.getText().isEmpty()) {
                    try {
                        String arg = topic.getText();
                        if (publisher.addTopic(arg) == true) {
                            topicList.setModel(new DefaultComboBoxModel(
                                    publisher.getTopics().toArray()
                            ));
                        }

                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        removeTopicButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(topicList.getSelectedItem()!=null) {

                    try {
                        publisher.removeTopic(topicList.getSelectedItem().toString());
                        topicList.setModel(new DefaultComboBoxModel(
                                publisher.getTopics().toArray()
                        ));
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        newsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!newsText.getText().isEmpty()) {
                    if(topicList.getSelectedItem()!=null) {

                        try {
                            publisher.topicNews(topicList.getSelectedItem().toString()
                                    ,newsText.getText());
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        });

        this.add(mainPanel);
        this.setResizable(false);
        this.pack();
        this.setSize(700,500);
        this.setVisible(true);

    }
    @Override
    protected void processWindowEvent(WindowEvent e){
        super.processWindowEvent(e);
        if(e.getID() == WindowEvent.WINDOW_CLOSING){

            try {
                publisher.removeAllTopics();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            publisher.closingPublisher();
            System.exit(0);
        }
    }
}
